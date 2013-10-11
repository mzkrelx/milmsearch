package models.mailsource.crawlers

import java.io.FileNotFoundException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale
import scala.io.Codec
import scala.io.Source
import scala.util.Try
import scala.xml.Node
import org.joda.time.DateTime
import javax.mail.internet.InternetAddress
import models.ML
import models.mailsource._
import play.api.Logger
import utils.HTMLUtil._
import utils.Regex
import scala.collection.mutable.ListBuffer

case class MailmanCrawlingException(msg: String) extends CrawlingException(msg)

object MailmanCrawler {

  val defaultCharset = "EUC-JP"

  /** Crawling test to check archiveURL is valid.
   *
   * @param  url ML archive URL
   * @return Success if the first mail is crawled; Failure otherwise
   */
  def crawlingTest(archiveURL: URL): Try[Mail] = {

    Try {
      try {
        val monthHrefs = collectMonthHref(toNode(fetchHTML(archiveURL)))

        val firstMonthHref = monthHrefs.reverse.headOption.getOrElse(
          throw MailmanCrawlingException("The archive month href could not be found."))

        val firstMonthURL = new URL(archiveURL + firstMonthHref)
        val mailHrefs = collectMailHref(toNode(fetchHTML(firstMonthURL)))

        val firstMailHref = mailHrefs.headOption.getOrElse(
          throw MailmanCrawlingException("The mail href could not be found."))

        val firstMailURL = new URL(firstMonthURL.toString.replaceFirst("date.html", firstMailHref))

        createMail(toMailHTMLNode(firstMailURL), firstMailURL)
      } catch {
        case e: FileNotFoundException => {
          throw MailmanCrawlingException("Not Found URL. => " + e.getMessage)
        }
      }
    }
  }

  def crawling(ml: ML) {

    val mailURLs = createMailURLs(createMonthURLs(ml.archiveURL))
    val stopURL = ML.findLastMail(ml.id).map(_.url.toString).getOrElse("NON-STOP")
    Logger.info(s"Crawling stop URL = [$stopURL]")

    val crawlingURLs = mailURLs takeWhile { !_.toString.equals(stopURL) }

    crawlingURLs foreach { mailURL =>
      val mail = createMail(toMailHTMLNode(mailURL), mailURL)
      Logger.debug("Crawled => " + mail.subject)

      Indexer.indexing(ml, mail)
    }
  }

  private def createMonthURLs(archiveURL: URL): Seq[URL] = {
    val monthHrefs = collectMonthHref(toNode(fetchHTML(archiveURL)))
    monthHrefs.map { href =>
      new URL(archiveURL + href)
    }
  }

  private def collectMonthHref(node: Node): Seq[String] = {
    node \\ "table" \\ "td" \\ "a" \\ "@href" map { _.toString } collect {
      case href if href.endsWith("/date.html") => href
    } distinct
  }

  /** Get all mail URLs order by date DESC */
  private def createMailURLs(monthURLs: Seq[URL]): Seq[URL] = {
    val mailURLs = ListBuffer[URL]()
    monthURLs foreach { monthURL =>
      val mailHrefs = collectMailHref(toNode(fetchHTML(monthURL)))
      val monthlyMailURLs = mailHrefs.map { href =>
        new URL(monthURL.toString.replaceFirst("date.html", href))
      }.reverse
      mailURLs ++= monthlyMailURLs
    }
    mailURLs.toList
  }

  private def collectMailHref(node: Node): Seq[String] = {
    val regexp = """^([0-9]+\.html)$""".r
    node \\ "ul" \ "li" \ "a" \\ "@href" map { _.toString } collect {
      case regexp(str) => str
    }
  }

  private def toMailHTMLNode(mailURL: URL): Node =
    try {
      val source = Source.fromURL(mailURL)(Codec(defaultCharset))
      val html = source.getLines map (Regex.ctrl.replaceAllIn(_, "")) mkString("\n")
      source.close
      toNode(html, defaultCharset)
    } catch {
      case e: Exception => {
        Logger.error(s"Can't fetch HTML => [$mailURL]")
        throw new CrawlingException(e.getMessage)
      }
    }

  private def createMail(mailHTMLNode: Node, mailURL: URL) = {
    Mail(
      findDate(mailHTMLNode),
      new InternetAddress(
        findFromAddress(mailHTMLNode),
        findFromName(mailHTMLNode)),
      findSubject(mailHTMLNode),
      findBody(mailHTMLNode),
      mailURL)
  }

  private def findDate(mailHTMLNode: Node): DateTime = {
    val format = "yyyy年 M月 d日 (EEE) HH:mm:ss z"
    val dateStr = (mailHTMLNode \\ "i").headOption.getOrElse {
      throw MailmanCrawlingException("The mail's date could not be found.")
    }.text

    // JodaTime doesn't support parsing time zone 'JST' Ver. 2.1 & 2.2
    new DateTime(new SimpleDateFormat(format, Locale.JAPAN).parse(dateStr))
  }

  private def findFromName(mailHTMLNode: Node): String = {
    (mailHTMLNode \\ "b").headOption.getOrElse {
      throw MailmanCrawlingException("The mail's from name could not be found.")
    }.text
  }

  private def findFromAddress(mailHTMLNode: Node): String = {
    (mailHTMLNode \\ "a").headOption.getOrElse {
      throw MailmanCrawlingException("The mail's from address could not be found.")
    }.text.trim.replaceAll(" ", "").replaceAll("＠", "@")
  }

  private def findSubject(mailHTMLNode: Node): String = {
    mailHTMLNode \\ "a" \\ "@title" match {
      case s if s.isEmpty => throw MailmanCrawlingException("The mail's subject could not be found.")
      case s => s.toString
    }
  }

  private def findBody(mailHTMLNode: Node): String = {
    (mailHTMLNode \\ "pre").headOption.getOrElse {
      throw MailmanCrawlingException("The mail's body could not be found.")
    }.text.trim
  }

}
