package models

import javax.mail.internet.InternetAddress
import org.joda.time.DateTime
import java.net.URL

case class Mail(
  date:     DateTime,
  fromAddr: InternetAddress,
  subject:  String,
  body:     String,
  snippet:  String,
  url:      URL,
  mlTitle:  String,
  mlURL:    URL)