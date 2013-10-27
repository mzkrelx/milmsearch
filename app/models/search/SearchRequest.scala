package models.search

import org.joda.time.DateTime

case class SearchRequest(
  keywords:     String,
  fromDate:     DateTime,
  toDate:       DateTime,
  fields:       Set[MailSearchField.Value],
  mlIDs:        Set[Long],
  froms:        Set[FromOption],
  itemsPerPage: Integer,
  order:        MailSearchOrder.Value,
  page:         Long) {

  def startIndex = page * itemsPerPage - itemsPerPage
}
