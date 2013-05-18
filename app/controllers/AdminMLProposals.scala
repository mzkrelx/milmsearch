package controllers

import play.api._
import play.api.mvc._
import models.MLProposal
import models.MLProposalStatus
import utils.BadRequestException
import Defaults.MaxItemsPerPage

object AdminMLProposals extends Controller {
  def list(statusParam: String, startIndex: Long, count: Int) = TryCatch4xx {
    Action { implicit request =>
      if (startIndex < 0 || count < 0) {
        throw BadRequestException("`startIndex` and `count` must be positive number.")
      }
      
      val status = statusParam match {
        case MLProposalStatus(x) => x
        case _ => throw BadRequestException("invalid `status` value.")
      }
      
      Ok(views.html.admin.mlproposals.list(
        MLProposal.list(
          startIndex, count min MaxItemsPerPage, status), status))
    }
  }
  
  def show(id: Long) = Action {
    NotImplemented
  }
}