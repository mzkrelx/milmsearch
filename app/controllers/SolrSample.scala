package controllers

import play.api._
import play.api.mvc._

import play.api.data._
import play.api.data.Forms._

import models.SolrSampleModels

object SolrSample extends Controller {

  val sampleForm = Form("data" -> nonEmptyText)
  def index(data: String) = Action {
    Ok(views.html.solrsample.index(SolrSampleModels.select(data), sampleForm))
  }

  def update = Action { implicit request =>
    sampleForm.bindFromRequest.fold(
      errors => BadRequest(views.html.index("*:*")),
      data => {
        print(data)
        SolrSampleModels.update(data)
        Redirect(routes.SolrSample.index("*:*"))
      }
    )
  }

  def delete(deleteId: String) = Action {
    SolrSampleModels.delete(deleteId)
    Redirect(routes.SolrSample.index("*:*"))
  }

  def search() = Action { implicit request =>
    sampleForm.bindFromRequest.fold(
      errors => BadRequest(views.html.index("*:*")),
      data => {
        print(data)
        Redirect(routes.SolrSample.index(data))
      }
    )
  }
}