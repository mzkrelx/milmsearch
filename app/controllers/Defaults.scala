package controllers

import org.joda.time.DateTime

/**
 * Constants of default values
 */
object Defaults {
  val ItemsPerPage = 10
  val MaxItemsPerPage = 100

  val searchDateFormat = "MM/dd/yyyy"

  def today = new DateTime().toString(searchDateFormat)
  def searchFrom = new DateTime(2012, 1, 1, 0, 0, 0).toString(searchDateFormat)
}