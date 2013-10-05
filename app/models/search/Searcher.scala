package models.search

import java.net.URL

import org.joda.time.DateTime

import javax.mail.internet.InternetAddress
import models._
import utils.MatchableEnumeration

case class SearchRequest(
  keywords:     String,
  fromDate:     DateTime,
  toDate:       DateTime,
  fields:       Set[MailSearchField.Value],
  mlIDs:        Set[Long],
  froms:        Set[FromOption],
  itemsPerPage: Integer,
  order:        MailSearchOrder.Value,
  page:         Long)

object MailSearchOrder extends MatchableEnumeration {
  type MailSearchOrder = Value
  val DateAsc  = Value("date_asc")
  val DateDesc = Value("date_desc")
}

case class SearchResult(
  totalResultCount: Long,
  startIndex: Long,
  itemsPerPage: Long,
  items: List[Mail])

case class MLOption(id: Long, title: String)

case class FromOption(name: String, email: Email) {

  def value = s"""$name${FromOption.valueSeparator}${email.toView}"""

  def label = s"""$name <${email.toView}>"""

}

object FromOption {

  val valueSeparator = ", "
  lazy val valueSeparatorLength = valueSeparator.length

  def apply(ia: InternetAddress): FromOption = {
    this(ia.getPersonal, Email(ia.getAddress))
  }

  def apply(viewValue: String): FromOption = {
    val nameEmail = viewValue.splitAt(viewValue.lastIndexOf(valueSeparator))
    this(nameEmail._1, Email.fromView(nameEmail._2.substring(valueSeparatorLength)))
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

    // TODO real search
    Page[Mail](
      (1 to 10) map ( i =>
        Mail(
          DateTime.now,
          new InternetAddress("dummy@example.com" + i, "dummy" + i),
          "タイトルが入ります" + i,
          "メール本文が入ります。メール本文が入ります。メール本文が入ります。",
          "メールスニペットが入ります。メールスニペットが入ります。メールスニペットが入ります。",
          new URL("http://example.com"),
          "MLタイトルが入ります" + i,
          new URL("http://example.com/ml"))
      ) toList,
      totalResultCount = 102,
      startIndex = req.page * req.itemsPerPage - req.itemsPerPage,
      itemsPerPage = req.itemsPerPage
    )
  }
}