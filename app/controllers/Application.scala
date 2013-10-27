package controllers

import play.api.mvc._

object Application extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  def inquiry = Action {
    Ok(views.html.inquiry())
  }

  def rule = Action {
    Ok(views.html.rule())
  }

  def policy = Action {
    Ok(views.html.policy())
  }

  def help = Action {
    Ok(views.html.help())
  }

}