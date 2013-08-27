package models

import utils.MatchableEnumeration

object MLProposalStatus extends MatchableEnumeration {
  type MLProposalStatus = Value
  val New      = Value("new")
  val Accepted = Value("accepted")
  val Rejected = Value("rejected")
}
