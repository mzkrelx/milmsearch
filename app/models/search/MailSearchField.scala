package models.search

object MailSearchField extends Enumeration {
  type MailSearchField = Value

  val MLTitle   = Value("mltitle")
  val Subject   = Value("subject")
  val Body      = Value("body")
  val From      = Value("from")
}