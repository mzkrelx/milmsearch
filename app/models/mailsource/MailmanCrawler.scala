package models.mailsource

import java.net.URL
import org.joda.time.DateTime
import javax.mail.internet.InternetAddress
import nu.validator.htmlparser.sax.HtmlParser
import scala.xml.parsing.NoBindingFactoryAdapter
import nu.validator.htmlparser.common.XmlViolationPolicy
import org.xml.sax.InputSource
import java.io.StringReader
import scala.xml.parsing.NoBindingFactoryAdapter
import scala.io.Source
import play.Logger
import org.joda.time.format.DateTimeFormat
import java.text.SimpleDateFormat
import java.util.Locale

object MailmanCrawler {

  /** Crawling test
   * @param  url ML archive URL
   * @return first mail
   */
  def test(url: URL): Mail = {

    val monthlyUrlNodeSeq = toNode(url) \\ "table" \\ "td" \\ "a" \\ "@href" map { _.toString } collect {
      case href if href.endsWith("/date.html") => href
    } distinct

    val firstMonthUrl = new URL(url + monthlyUrlNodeSeq.reverse.head.toString)

    val regexp = """^([0-9]+\.html)$""".r
    val mailUrlNodeSeq = toNode(firstMonthUrl) \\ "ul" \ "li" \ "a" \\ "@href" map { _.toString } collect {
      case regexp(str) => str
    }

    val firstMailURL = new URL(firstMonthUrl.toString().replaceFirst("date.html", mailUrlNodeSeq.head))

    val mailNode = toNode(firstMailURL)

    Mail(
     date = new DateTime(new SimpleDateFormat("yyyy年 M月 d日 (EEE) HH:mm:ss z", Locale.JAPAN).parse((mailNode \\ "p" \ "i").head.text)),
     fromAddr = new InternetAddress((mailNode \\ "p" \ "a").head.text.trim().replaceAll(" ", "").replaceAll("＠", "@"),
         (mailNode \\ "p" \ "b").head.text),
     subject = mailNode \\ "p" \ "a" \\ "@title" toString(),
     body = (mailNode \\ "pre").head.text.trim,
     srcURL = firstMailURL)
  }

  def toNode(url: URL) = {
    val htmlParser = new HtmlParser
    htmlParser.setNamePolicy(XmlViolationPolicy.ALLOW)

    val contentHandler = new NoBindingFactoryAdapter
    htmlParser.setContentHandler(contentHandler)
    htmlParser.parse(new InputSource(url.openStream()))

    contentHandler.rootElem
  }
}