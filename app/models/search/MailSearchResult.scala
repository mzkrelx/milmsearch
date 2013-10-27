package models.search

import models.Mail
import models.Page

case class MailSearchResult(
  page: Page[Mail],
  mlOptions: List[MLOption],
  fromOptions: List[FromOption]
)
