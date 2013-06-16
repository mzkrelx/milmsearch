package models.mailsource

import org.joda.time.DateTime
import java.net.URL
import javax.mail.internet.InternetAddress

case class Mail(
  date:     DateTime,
  fromAddr: InternetAddress,
  subject:  String,
  body:     String,
  srcURL:   URL)
