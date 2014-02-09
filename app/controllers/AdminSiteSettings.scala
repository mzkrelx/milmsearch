package controllers

import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import models.SiteSetting

object AdminSiteSettings extends Controller {

  val form = Form(
    mapping(
      "footerHtml"   -> optional(text)
    ){
      (footerHtml) => SiteSetting(footerHtml.getOrElse(""))
    }{
      (siteSetting: SiteSetting) => Some(Some(siteSetting.footerHtml))
    }
  )

  def showEditForm = Action { implicit request =>
    Ok(views.html.admin.sitesettings.editForm(
        form.fill(SiteSetting.find), flash.get("success").getOrElse("")))
  }

  def submitEditForm = Action { implicit request =>
    form.bindFromRequest.fold(
      errorForm => BadRequest(views.html.admin.sitesettings.editForm(errorForm)),
      siteSetting => {
        SiteSetting.update(siteSetting)
        Redirect(routes.AdminSiteSettings.showEditForm).flashing(
          "success" -> "保存しました")
      }
    )
  }
}