package models.mailsource

import org.joda.time.DateTime
import java.net.URL
import javax.mail.internet.InternetAddress

case class Mail(
  date:    DateTime,
  address: InternetAddress,
  subject: String,
  text:    String,
  url:     URL)
