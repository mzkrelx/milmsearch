package controllers

import play.api._
import play.api.mvc._
import models.MLProposal
import models.MLProposalUpdateRequest
import models.{MLProposalStatus => MLPStatus}
import models.MLArchiveType
import utils.BadRequestException
import Defaults.MaxItemsPerPage
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints.pattern
import java.rmi.UnexpectedException
import java.net.URL
import anorm._

object AdminMLProposals extends Controller {
  
  val editForm = Form(
    mapping(
      "id"          -> ignored(NotAssigned:Pk[Long]),
      "mlTitle"     -> nonEmptyText(maxLength = 100),
      "archiveType" -> nonEmptyText.verifying(
        "invalid archive type", MLArchiveType.unapply(_).isDefined),
      "archiveURL"  -> nonEmptyText(maxLength = 200).verifying(
        pattern("""^https?://[^/]+/.*$""".r))
    ) { (id, title, arcType, arcURL) => 
      MLProposalUpdateRequest(
        id, title, MLArchiveType.unapply(arcType).get, new URL(arcURL))
    } { req =>
      Some(req.id, req.mlTitle, req.archiveType.toString, req.archiveURL.toString)
    }
  )
  
  val judgeForm = Form(
    single("status" -> nonEmptyText.verifying(
      pattern(("^(%s|%s)$" format (MLPStatus.Accepted, MLPStatus.Rejected)).r))))
      
  def testCrawling(id: Long) = Action { // findでMLのURLを持ってきて、Modelに渡す
    MLProposal.find(id) map { mlp =>
      Ok(views.html.admin.mlproposals.show(mlp))
    } getOrElse NotFound
  }
    
  def list(statusParam: String, startIndex: Long, count: Int) = TryCatch4xx {
    Action { implicit request =>
      if (startIndex < 0 || count < 0) {
        throw BadRequestException("`startIndex` and `count` must be positive number.")
      }
      
      val status = statusParam match {
        case MLPStatus(x) => x
        case _ => throw BadRequestException("invalid `status` value.")
      }
      
      Ok(views.html.admin.mlproposals.list(
        MLProposal.list(
          startIndex, count min MaxItemsPerPage, status), status))
    }
  }
  
  def show(id: Long) = Action {
    MLProposal.find(id) map { mlp =>
      Ok(views.html.admin.mlproposals.show(mlp))
    } getOrElse NotFound
  }
  
  def showEditForm(id: Long) = Action {
    MLProposal.find(id) map { mlp =>
      Ok(views.html.admin.mlproposals.editForm(id,
        editForm.fill(mlp.asUpdateRequest)))
    } getOrElse NotFound
  }
  
  def submitEditForm(id: Long) = Action { implicit request =>   
    editForm.bindFromRequest.fold(
      errors => BadRequest(views.html.admin.mlproposals.editForm(id, errors)),
      req    => {
        anorm.Success
        MLProposal.update(req.copy(id = Id(id)))
        Redirect(routes.AdminMLProposals.show(id))
      }
    )
  }
  
  def showJudgeConfirm(id: Long, statusToParam: String) = TryCatch4xx {
    Action {
      val statusTo = statusToParam match {
        case MLPStatus(x) if
          (x == MLPStatus.Accepted || x == MLPStatus.Rejected) => x
        case _ => throw BadRequestException("invalid `status` value.")
      }
      
      MLProposal.find(id) map { mlp =>
        Ok(views.html.admin.mlproposals.judgeConfirm(mlp, statusTo))
      } getOrElse (throw BadRequestException("resource not found."))
    }
  }
  
  def judge(id: Long) = Action { implicit request =>
    judgeForm.bindFromRequest.fold(
      errors => BadRequest("invalid `status` value."),
      status => status match {
        case MLPStatus(statusTo) =>  {
          try {
            MLProposal.judge(id, statusTo)
            Logger.debug(s"ml proposal has updated. [id=${id}, status=${statusTo}]")
            Ok
          } catch {
            case e: UnexpectedException => BadRequest(e.getMessage) 
            case e: Throwable => {
              Logger.error(e.getMessage, e)
              InternalServerError("error occured. please contact webmaster.")
            }
          }
        }
      }
    )
  }
  
  def showJudgeCompleted(id: Long, statusToParam: String) = TryCatch4xx {
    Action {
      val statusTo = statusToParam match {
        case MLPStatus(x) => x
        case _ => throw BadRequestException("invalid `status` value.")
      }
        
      MLProposal.find(id) map { mlp =>
        Ok(views.html.admin.mlproposals.judgeCompleted(mlp, statusTo))
      } getOrElse (throw BadRequestException("resource not found."))
    }
  }
}
