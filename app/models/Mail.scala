package models

import javax.mail.internet.InternetAddress
import org.joda.time.DateTime
import java.net.URL

case class Mail(
  date:     DateTime,
  fromAddr: InternetAddress,
  subject:  String,
  snippet:  String,
  url:      URL,
  mlTitle:  String,
  mlURL:    URL)

case class MailAddress(mailAddress: String) {
  def toView = mailAddress.replaceFirst("@", " ＠ ")
  override def toString = mailAddress
}

object MailAddress {
  def fromView(viewMailAddress: String): MailAddress =
    MailAddress(viewMailAddress.replaceFirst(" ＠ ", "@"))
}
