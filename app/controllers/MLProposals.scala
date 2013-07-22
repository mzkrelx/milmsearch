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

  val form = Form(
    mapping(
      "proposerName"    -> nonEmptyText,
      "proposerEmail"   -> nonEmptyText,
      "proposerEmail2"  -> nonEmptyText,
      "mlTitle"         -> nonEmptyText,
      "archiveType"     -> nonEmptyText,
      "archiveURL"      -> nonEmptyText,
      "message"         -> optional(text),
      "agreement"       -> checked("利用規約に同意する")
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
        false))
    }
  )

  def create = Action {
    Ok(views.html.mlproposals.createForm(form))
  }

  def confirm = Action {
    Ok(views.html.mlproposals.createConfirm())
  }

  def save = Action {
    Ok(views.html.mlproposals.createComplete())
  }

}