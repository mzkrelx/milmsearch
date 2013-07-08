package controllers

import play.api._
import play.api.mvc._
import models.MLProposal

object MLs extends Controller {

  def list = Action {
    Ok(views.html.mls.list())
  }

}