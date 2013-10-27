package controllers
import java.net.URL

import org.joda.time.DateTime

import anorm.NotAssigned
import models._
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._

object MLProposals extends Controller {

  val mlpForm = Form(
    mapping(
      "proposerEmail"   -> optional(text(maxLength = 100)),
      "proposerEmail2"  -> optional(text(maxLength = 100)),
      "mlTitle"         -> nonEmptyText(maxLength = 100),
      "archiveURL"      -> nonEmptyText(maxLength = 100),
      "message"         -> optional(text(maxLength = 200)),
      "agreement"       -> nonEmptyText(maxLength = 2) // Not checked type, because multilingual correspondence of error message is simple.
    ){
      (proposerEmail, _, mlTitle, archiveURL, message, _) =>
          MLProposal(
            id = NotAssigned,
            proposerEmail,
            mlTitle,
            status = MLProposalStatus.New,
            MLArchiveType.Mailman,
            new URL(archiveURL),
            message.getOrElse(""),
            judgedAt = None,
            createdAt = DateTime.now(),
            updatedAt = DateTime.now())
    }{
      (mlp: MLProposal) => Some((
        mlp.proposerEmail,
        mlp.proposerEmail,
        mlp.mlTitle,
        mlp.archiveURL.toString,
        Option(mlp.message),
        "on"))
    }
  )

  /** Show create form. */
  def create = Action {
    Ok(views.html.mlproposals.createForm(mlpForm))
  }

  /** Show filled create form. */
  def modify = Action { implicit request =>
    Ok(views.html.mlproposals.createForm(mlpForm.bindFromRequest))
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