package utils

import play.api.Play


/**
 * pattern matchable enumeration
 */
trait MatchableEnumeration extends Enumeration {
  def unapply(s: String): Option[Value] =
    values.find(s == _.toString)
}

object Utils {
  def playConfig = Play.configuration(Play.current)
}