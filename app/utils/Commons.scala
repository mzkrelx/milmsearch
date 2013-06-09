package utils

/**
 * pattern matchable enumeration
 */
trait MatchableEnumeration extends Enumeration {
  def unapply(s: String): Option[Value] =
    values.find(s == _.toString)
}