package controllers

import play.api._
import play.api.mvc._
import models.MLProposal
import play.api.data._
import play.api.data.Forms._
import anorm.Pk
import anorm.NotAssigned
import models.MLProposalStatus
import org.joda.time.DateTime
import models.MLArchiveType
import java.net.URL

object MLProposals extends Controller {

  val mlpForm = Form(
    mapping(
      "proposerName"    -> nonEmptyText(maxLength = 100),
      "proposerEmail"   -> nonEmptyText(maxLength = 100),
      "proposerEmail2"  -> nonEmptyText(maxLength = 100),
      "mlTitle"         -> nonEmptyText(maxLength = 100),
      "archiveType"     -> nonEmptyText(maxLength = 20),
      "archiveURL"      -> nonEmptyText(maxLength = 100),
      "message"         -> optional(text(maxLength = 200)),
      "agreement"       -> nonEmptyText(maxLength = 2) // Not checked type, because multilingual correspondence of error message is simple.
    ){
      (proposerName, proposerEmail, _, mlTitle, archiveType,
        archiveURL, message, _) =>
          MLProposal(
            id = NotAssigned,
            proposerName,
            proposerEmail,
            mlTitle,
            status = MLProposalStatus.New,
            MLArchiveType.withName(archiveType),
            new URL(archiveURL),
            message.getOrElse(""),
            judgedAt = None,
            createdAt = DateTime.now(),
            updatedAt = DateTime.now())
    }{
      (mlp: MLProposal) => Some((
        mlp.proposerName,
        mlp.proposerEmail,
        mlp.proposerEmail,
        mlp.mlTitle,
        mlp.archiveType.toString,
        mlp.archiveURL.toString,
        Option(mlp.message),
        "on"))
    }
  )

  /** Show create form. */
  def create = Action {
    Ok(views.html.mlproposals.createForm(mlpForm))
  }

  def confirm = Action { implicit request =>
    mlpForm.bindFromRequest.fold(
      errorForm => BadRequest(views.html.mlproposals.createForm(errorForm)),
      _ => {
        Ok(views.html.mlproposals.createConfirm(mlpForm.bindFromRequest))
      }
    )
  }

  def save = Action { implicit request =>
    mlpForm.bindFromRequest.fold(
      errorForm => BadRequest(views.html.mlproposals.createForm(errorForm)),
      mlp => {
        MLProposal.save(mlp)
        Ok(views.html.mlproposals.createComplete())
      }
    )
  }

}