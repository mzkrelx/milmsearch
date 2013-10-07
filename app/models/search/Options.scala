package models.search

import javax.mail.internet.InternetAddress
import models.MailAddress
import utils.MatchableEnumeration

object MailSearchOrder extends MatchableEnumeration {
  type MailSearchOrder = Value
  val DateAsc  = Value("date_asc")
  val DateDesc = Value("date_desc")
}

case class MLOption(id: Long, title: String)

case class FromOption(name: String, email: MailAddress) {

  def value = s"""$name${FromOption.valueSeparator}${email.toView}"""

  def label = s"""$name <${email.toView}>"""

}

object FromOption {

  val valueSeparator = ", "
  lazy val valueSeparatorLength = valueSeparator.length

  def apply(ia: InternetAddress): FromOption = {
    this(ia.getPersonal, MailAddress(ia.getAddress))
  }

  def apply(viewValue: String): FromOption = {
    val nameEmail = viewValue.splitAt(viewValue.lastIndexOf(valueSeparator))
    this(nameEmail._1, MailAddress.fromView(nameEmail._2.substring(valueSeparatorLength)))
  }

}
