package controllers

import play.api._
import play.api.mvc._

object Admin extends Controller {
  
  def index = Action {
    Ok(views.html.admin.index())
  }
  
  def listMLProposals = Action {
    Ok(views.html.admin.listMLProposals())
  }
  
}