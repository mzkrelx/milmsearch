package controllers

import play.api._
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

  def poricy = Action {
    Ok(views.html.poricy())
  }

  def help = Action {
    Ok(views.html.help())
  }

  def search(/* TODO */) = Action {
    Ok(views.html.searchResult())
  }

}