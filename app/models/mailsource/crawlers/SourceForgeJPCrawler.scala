package models.mailsource.crawlers

import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale

import scala.util.Try
import scala.xml.Node

import org.joda.time.DateTime

import javax.mail.internet.InternetAddress
import models.mailsource.Indexer
import models.ML
import models.mailsource.CrawlingException
import models.mailsource.Mail
import utils.HTMLUtil.fetchHTML
import utils.HTMLUtil.toNode

case class SourceForgeJPCrawlingException(msg: String) extends CrawlingException(msg)

object SourceForgeJPCrawler {

  /** Crawling test to check archiveURL is valid.
   *
   * @param  url ML archive URL
   *   e.g. "http://sourceforge.jp/projects/milm-search/lists/archive/public/"
   * @return Success if the first mail is crawled; Failure otherwise
   */
  def crawlingTest(archiveURL: URL): Try[Mail] = {

    Try {

      val monthHrefs = collectMonthHref(toNode(fetchHTML(archiveURL)))

      // e.g. 2011-August/date.html
      val firstMonthHref = monthHrefs.reverse.headOption.getOrElse(
        throw SourceForgeJPCrawlingException("The archive month href could not be found."))

      // e.g. "http://sourceforge.jp/projects/milm-search/lists/archive/public/2011-August/date.html"
      val firstMonthURL = new URL(archiveURL + firstMonthHref)
      val mailHrefs = collectMailHref(toNode(fetchHTML(firstMonthURL)))

      // e.g. "000000.html"
      val firstMailHref = mailHrefs.headOption.getOrElse(
          throw SourceForgeJPCrawlingException("The mail href could not be found."))

      // e.g. "http://sourceforge.jp/projects/milm-search/lists/archive/public/2011-August/000000.html"
      val firstMailURL = new URL(firstMonthURL.toString.replaceFirst("date.html", firstMailHref))
      val mailHTMLNode = toNode(fetchHTML(firstMailURL))

      createMail(toNode(fetchHTML(firstMailURL)), firstMailURL)
    }
  }

  def crawling(ml: ML) {

    val monthHrefs = collectMonthHref(toNode(fetchHTML(ml.archiveURL)))
    val monthURLs = monthHrefs.reverse.map { href =>
      new URL(ml.archiveURL.toString + href)
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

  private def findDate(mailHTMLNode: Node): DateTime = {
    val format = "yyyy年 M月 d日 (EEE) HH:mm:ss z"
    val dateStr = (mailHTMLNode \\ "p" \ "i").headOption.getOrElse {
      throw SourceForgeJPCrawlingException("The mail's date could not be found.")
    }.text

    // JodaTime doesn't support parsing time zone 'JST' Ver. 2.1 & 2.2
    new DateTime(new SimpleDateFormat(format, Locale.JAPAN).parse(dateStr))
  }

  private def findFromName(mailHTMLNode: Node): String = {
    (mailHTMLNode \\ "p" \ "b").headOption.getOrElse {
      throw SourceForgeJPCrawlingException("The mail's from name could not be found.")
    }.text
  }

  private def findFromAddress(mailHTMLNode: Node): String = {
    (mailHTMLNode \\ "p" \ "a").headOption.getOrElse {
      throw SourceForgeJPCrawlingException("The mail's from address could not be found.")
    }.text.trim.replaceAll(" ", "").replaceAll("＠", "@")
  }

  private def findSubject(mailHTMLNode: Node): String = {
    mailHTMLNode \\ "p" \ "a" \\ "@title" match {
      case s if s.isEmpty => throw SourceForgeJPCrawlingException("The mail's subject could not be found.")
      case s => s.toString
    }
  }

  private def findBody(mailHTMLNode: Node): String = {
    (mailHTMLNode \\ "pre").headOption.getOrElse {
      throw SourceForgeJPCrawlingException("The mail's body could not be found.")
    }.text.trim
  }

}
