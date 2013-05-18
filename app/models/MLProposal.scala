package models

import org.joda.time.DateTime
import play.api.db.DB
import play.api.Play.current
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

object MLArchiveType extends Enumeration {
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
  updatedAt:     DateTime)

object MLProposal {
  val DBTableName = "ml_proposal"
  
  def create(mlp: MLProposal) {
    DB.withConnection { implicit connection =>
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
    DB.withConnection { implicit connection =>
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
              'status -> status.toString())().head[Long]("c")
     
      Page(items.toList, totalResults, startIndex, itemsPerPage)
    }
  }
}

