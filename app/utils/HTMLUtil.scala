package utils

import java.net.URL
import scala.xml.Node
import scala.xml.parsing.NoBindingFactoryAdapter
import org.xml.sax.InputSource
import nu.validator.htmlparser.common.XmlViolationPolicy
import nu.validator.htmlparser.sax.HtmlParser

object HTMLUtil {

  /** Returns InputSource to read the URL.
   *
   * @param url the URL to read
   */
  def fetchHTML(url: URL): InputSource = {
    new InputSource(url.openStream())
  }

  /** Returns XML Node Object by parsing of HTML InputSource.
   *
   * @param is the InputSource
   */
  def toNode(is: InputSource): Node = {
    val htmlParser = new HtmlParser
    htmlParser.setNamePolicy(XmlViolationPolicy.ALLOW)

    val contentHandler = new NoBindingFactoryAdapter
    htmlParser.setContentHandler(contentHandler)
    htmlParser.parse(is)

    contentHandler.rootElem
  }
}