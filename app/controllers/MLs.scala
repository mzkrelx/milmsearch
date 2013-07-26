package controllers

import play.api._
import play.api.mvc._
import controllers.Defaults._
import models.MLProposal
import models.{MLProposalStatus => MLPStatus}
import utils.BadRequestException
import models.ML

object MLs extends Controller {

  def list(startIndex: Long, count: Int) = TryCatch4xx {
    Action { implicit request =>

      if (startIndex < 0 || count < 0) {
        throw BadRequestException("`startIndex` and `count` must be positive number.")
      }

      val itemCountPerPage = count min MaxItemsPerPage
      val mls = ML.list(startIndex, itemCountPerPage)

      Ok(views.html.mls.list(
        Page(mls, ML.totalResultCount, startIndex, itemCountPerPage)
      ))
    }
  }

}