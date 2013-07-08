package controllers

import play.api._
import play.api.mvc._
import models.MLProposal
import play.api.data._
import play.api.data.Forms._
import anorm.Pk
import anorm.NotAssigned

object MLProposals extends Controller {

  val form = Form(
    tuple(
      "id"              -> ignored(NotAssigned:Pk[Long]),
      "proposerName"    -> nonEmptyText,
      "proposerEmail"   -> nonEmptyText,
      "mlTitle"         -> nonEmptyText,
      "status"          -> nonEmptyText,
      "archiveType"     -> nonEmptyText,
      "archiveURL"      -> nonEmptyText,
      "message"         -> optional(text)
    )
  )

  def create = Action {
    Ok(views.html.mlproposals.createForm(form))
  }

  def confirm = Action {
    Ok(views.html.mlproposals.createConfirm())
  }

  def save = Action {
    Ok(views.html.mlproposals.createComplete())
  }

}