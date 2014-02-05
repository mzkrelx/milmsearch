package controllers

import play.api.mvc._

object AdminSiteSettings extends Controller {

  def showEditForm() = Action {
    Ok(views.html.admin.sitesettings.editForm())
  }
}