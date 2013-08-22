package models.mailsource.crawlers

import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale
import scala.util.Try
import scala.xml.Node
import org.joda.time.DateTime
import javax.mail.internet.InternetAddress
import models.ML
import models.mailsource.CrawlingException
import models.mailsource.Mail
import utils.HTMLUtil.fetchHTML
import utils.HTMLUtil.toNode
import java.sql.Connection
import models.MLProposal
import play.Logger
import utils.HTMLUtil
import models.Indexer

case class MailmanCrawlingException(msg: String) extends CrawlingException(msg)

object MailmanCrawler {

  /** Crawling test to check archiveURL is valid.
   *
   * @param  url ML archive URL
   * @return Success if the first mail is crawled; Failure otherwise
   */
  def crawlingTest(archiveURL: URL): Try[Mail] = {

    Try {

      val monthHrefs = collectMonthHref(toNode(fetchHTML(archiveURL)))

      val firstMonthHref = monthHrefs.reverse.headOption.getOrElse(
        throw MailmanCrawlingException("The archive month href could not be found."))

      val firstMonthURL = new URL(archiveURL + firstMonthHref)
      val mailHrefs = collectMailHref(toNode(fetchHTML(firstMonthURL)))

      val firstMailHref = mailHrefs.headOption.getOrElse(
          throw MailmanCrawlingException("The mail href could not be found."))

      val firstMailURL = new URL(firstMonthURL.toString.replaceFirst("date.html", firstMailHref))

      createMail(toNode(fetchHTML(firstMailURL)), firstMailURL)
    }
  }

  def clawling(archiveURL: URL) {
    import HTMLUtil._

    val monthHrefs = collectMonthHref(toNode(fetchHTML(archiveURL)))
    val monthURLs = monthHrefs.reverse.map { href =>
      new URL(archiveURL + href)
    }

    monthURLs foreach { monthURL =>
      val mailHrefs = collectMailHref(toNode(fetchHTML(monthURL)))
      val mailURLs = mailHrefs map { href =>
        new URL(monthURL.toString.replaceFirst("date.html", href))
      }

      val mails = mailURLs map { mailURL =>
        createMail(toNode(fetchHTML(mailURL)), mailURL)
      }
      // TODO  save to search server
      // mails foreach { mail =>
    }
  }

  def crawling(ml: ML) {
    
    val archiveURL = ml.archiveURL
    
    val monthHrefs = collectMonthHref(toNode(fetchHTML(archiveURL)))
    val monthURLs = monthHrefs.reverse.map { href =>
      new URL(archiveURL + href)
    }

    monthURLs foreach { monthURL =>
      val mailHrefs = collectMailHref(toNode(fetchHTML(monthURL)))
      val mailURLs = mailHrefs map { href =>
        new URL(monthURL.toString.replaceFirst("date.html", href))
      }

      mailURLs map { mailURL =>
        Indexer.indexing(ml,createMail(toNode(fetchHTML(mailURL)), mailURL))
      } 
    }
  }
  
  private def collectMonthHref(node: Node): Seq[String] = {
    node \\ "table" \\ "td" \\ "a" \\ "@href" map { _.toString } collect {
      case href if href.endsWith("/date.html") => href
    } distinct
  }

  private def collectMailHref(node: Node): Seq[String] = {
    val regexp = """^([0-9]+\.html)$""".r
    node \\ "ul" \ "li" \ "a" \\ "@href" map { _.toString } collect {
      case regexp(str) => str
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
