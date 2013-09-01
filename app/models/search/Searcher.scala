package models.search

import org.joda.time.DateTime
import models.Mail
import javax.mail.internet.InternetAddress
import java.net.URL
import utils.Page

case class SearchRequest(
  keywords: String,
  fromDate: DateTime,
  toDate:   DateTime,
  fields:   Set[MailSearchField.Value])

case class SearchResult(
  totalResults: Long,
  startIndex: Long,
  itemsPerPage: Long,
  items: List[Mail])

case class MLOption(id: Long, title: String)

case class FromOption(name: String, email: Email) {

  def value = s"""$name, ${email.toView}"""

  def label = s"""$name <${email.toView}>"""

}

object FromOption {

  def apply(ia: InternetAddress): FromOption = {
    this(ia.getPersonal, Email(ia.getAddress))
  }

  def apply(name: String, email: String) {
    this(name, Email(email))
  }

  def apply(value: String) {
    val nameEmail = value.splitAt(value.lastIndexOf(", "))
    this(nameEmail._1, Email.fromView(nameEmail._2))
  }

}

case class Email(email: String) {
  def toView = email.replaceFirst("@", " ＠ ")
}

object Email {
  def fromView(viewEmail: String): Email =
    Email(viewEmail.replaceFirst(" ＠ ", "@"))
}

object Searcher {
  def search(req: SearchRequest): Page[Mail] = {
    Page[Mail](
      (1 to 10) map ( i =>
        Mail(
          DateTime.now,
          new InternetAddress("dummy@example.com", "dummy"),
          "タイトルが入ります" + i,
          "メール本文が入ります。メール本文が入ります。メール本文が入ります。",
          new URL("http://example.com"),
          "MLタイトルが入ります",
          new URL("http://example.com/ml"))
      ) toList,
      31, 0, 10
    )
  }
}