package controllers

import play.api._
import play.api.mvc._
import models.MLProposal

object Admin extends Controller {
  
  def index = Action {
    Ok(views.html.admin.index())
  }
  
  def javascriptRoutes = Action { implicit request =>
    Ok(Routes.javascriptRouter("jsRouter", Some("jQuery.ajax"))(
      routes.javascript.AdminMLProposals.judge,
      routes.javascript.AdminMLProposals.testCrawling)
    ).as("text/javascript")
  }
  
}