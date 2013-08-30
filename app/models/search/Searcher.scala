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