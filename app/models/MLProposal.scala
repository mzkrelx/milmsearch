package models

import play.api.UnexpectedException
import play.api.db.DB
import play.api.Play.current
import org.joda.time.DateTime
import anorm.Pk
import anorm.SQL
import java.util.Date
import java.net.URL
import utils.MatchableEnumeration
import controllers.Page

object MLProposalStatus extends MatchableEnumeration {
  type MLProposalStatus = Value
  val New      = Value("new")
  val Accepted = Value("accepted")
  val Rejected = Value("rejected")
}
import MLProposalStatus._

object MLArchiveType extends MatchableEnumeration {
  type MLArchiveType = Value
  val Mailman = Value("mailman")
  val Other   = Value("other")
}
import MLArchiveType._

case class MLProposal(
  id: Pk[Long],
  proposerName:  String,
  proposerEmail: String,
  mlTitle:       String,
  status:        MLProposalStatus,
  archiveType:   MLArchiveType,
  archiveURL:    URL,
  message:       String,
  judgedAt:      Option[DateTime],
  createdAt:     DateTime,
  updatedAt:     DateTime) {

  def asUpdateRequest =
    MLProposalUpdateRequest(id, mlTitle, archiveType, archiveURL)
}

case class MLProposalUpdateRequest(
  id:          Pk[Long],
  mlTitle:     String,
  archiveType: MLArchiveType,
  archiveURL:  URL)

object MLProposal {
  val DBTableName = "ml_proposal"

  def save(mlp: MLProposal) {
    DB.withConnection { implicit conn =>
      SQL(s"""
        INSERT INTO ${DBTableName}
          VALUES(
            nextval('ml_proposal_id_seq'),
            {proposer_name},
            {proposer_email},
            {ml_title},
            {status},
            {archive_type},
            {archive_url},
            {message},
            null,
            {created_at},
            {updated_at})"""
      ).on(
        'proposer_name  -> mlp.proposerName,
        'proposer_email -> mlp.proposerEmail,
        'ml_title       -> mlp.mlTitle,
        'status         -> mlp.status.toString,
        'archive_type   -> mlp.archiveType.toString,
        'archive_url    -> mlp.archiveURL.toString,
        'message        -> mlp.message,
        'created_at     -> mlp.createdAt.toDate,
        'updated_at     -> mlp.updatedAt.toDate
      ).executeInsert()
    }
  }

  def list(startIndex: Long, itemsPerPage: Int,
           status: MLProposalStatus) = {
    DB.withConnection { implicit conn =>
      val items =
        SQL(s"""
          SELECT * FROM ${DBTableName}
            WHERE status = {status}
            ORDER BY created_at
            LIMIT {limit} OFFSET {offset}"""
        ).on(
          'status -> status.toString(),
          'offset -> startIndex,
          'limit  -> itemsPerPage
        ).apply() map { row =>
          MLProposal(
            row[Pk[Long]]("id"),
            row[String]("proposer_name"),
            row[String]("proposer_email"),
            row[String]("ml_title"),
            MLProposalStatus.withName(row[String]("status")),
            MLArchiveType.withName(row[String]("archive_type")),
            new URL(row[String]("archive_url")),
            row[String]("message"),
            row[Option[Date]]("judged_at") map { new DateTime(_) },
            new DateTime(row[Date]("created_at")),
            new DateTime(row[Date]("updated_at")))
        }

      val totalResults =
        SQL(s"""
          SELECT COUNT(*) AS "c" FROM ${DBTableName}
            WHERE status = {status}""").on(
              'status -> status.toString)().head[Long]("c")

      Page(items.toList, totalResults, startIndex, itemsPerPage)
    }
  }

  def find(id: Long): Option[MLProposal] =
    DB.withConnection { implicit conn =>
      SQL(s"SELECT * FROM ${DBTableName} WHERE id = {id}")
        .on('id -> id).singleOpt map { row =>
          MLProposal(
            row[Pk[Long]]("id"),
            row[String]("proposer_name"),
            row[String]("proposer_email"),
            row[String]("ml_title"),
            MLProposalStatus.withName(row[String]("status")),
            MLArchiveType.withName(row[String]("archive_type")),
            new URL(row[String]("archive_url")),
            row[String]("message"),
            row[Option[Date]]("judged_at") map { new DateTime(_) },
            new DateTime(row[Date]("created_at")),
            new DateTime(row[Date]("updated_at")))
        }
    }

  def update(req: MLProposalUpdateRequest) {
    DB.withConnection { implicit conn =>
      SQL(s"""
        UPDATE ${DBTableName} SET
            ml_title     = {ml_title},
            archive_type = {archive_type},
            archive_url  = {archive_url},
            updated_at   = current_timestamp
          WHERE id = {id}""")
        .on(
          'ml_title     -> req.mlTitle,
          'archive_type -> req.archiveType.toString,
          'archive_url  -> req.archiveURL.toString,
          'id           -> req.id).executeUpdate()
    }
  }

  def judge(id: Long, statusTo: MLProposalStatus) {
    DB.withTransaction { implicit conn =>
      SQL(s"SELECT status FROM ${DBTableName} WHERE id = {id} FOR UPDATE")
        .on('id -> id).singleOpt.map(_[String]("status")) match {
          case None => throw UnexpectedException(Some("record not found."))
          case Some(status) if (status != New.toString) =>
            throw UnexpectedException(Some("already judged."))
          case _ =>
        }

      SQL(s"""
        UPDATE ${DBTableName}
          SET status     = {status},
              judged_at  = current_timestamp,
              updated_at = current_timestamp WHERE id = {id}""")
        .on('status -> statusTo.toString, 'id -> id).executeUpdate()
    }
  }
}
