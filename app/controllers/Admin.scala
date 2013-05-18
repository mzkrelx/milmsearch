package controllers

import play.api._
import play.api.mvc._
import models.MLProposal

object Admin extends Controller {
  
  def index = Action {
    Ok(views.html.admin.index())
  }
  
}