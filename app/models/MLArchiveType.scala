package models

import utils.MatchableEnumeration

object MLArchiveType extends MatchableEnumeration {
  type MLArchiveType = Value
  val Mailman       = Value("mailman")
  val SourceForgeJP = Value("sourceforgejp")
  val Other         = Value("other")
}
