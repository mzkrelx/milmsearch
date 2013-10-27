package utils

import play.api.Play

case class ConfigException(msg: String) extends Exception(msg)

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