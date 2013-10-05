package models

import play.api.db.DB
import play.api.Play.current
import anorm.SQL
import org.joda.time.DateTime
import java.net.URL
import MLArchiveType._
import java.util.Date
import java.sql.Connection

case class ML(
  id: Long,
  mlTitle:       String,
  archiveType:   MLArchiveType,
  archiveURL:    URL,
  judgedAt:      DateTime,
  lastMailedAt:  Option[DateTime])

object ML {

  def list(startIndex: Long, itemsPerPage: Int): List[ML] = {

    DB.withConnection { implicit conn =>
      val items =
        SQL(s"""
          SELECT * FROM ml_proposal
            WHERE status = {status}
            ORDER BY ml_title
            LIMIT {limit} OFFSET {offset}"""
        ).on(
          'status -> MLProposalStatus.Accepted.toString,
          'offset -> startIndex,
          'limit  -> itemsPerPage
        ).apply() map { row =>
          val id = row[Long]("id")
          lazy val lastMailedAt = this.lastMailedAt(id)
          ML(
            id,
            row[String]("ml_title"),
            MLArchiveType.withName(row[String]("archive_type")),
            new URL(row[String]("archive_url")),
            new DateTime(row[Date]("judged_at")),
            lastMailedAt)
        }
      items.toList
    }
  }

  def totalResultCount(): Long = {

    DB.withConnection { implicit conn =>
      SQL(s"""
        SELECT COUNT(*) AS "c" FROM ml_proposal
          WHERE status = {status}"""
      ).on(
        'status -> MLProposalStatus.Accepted.toString
      )().head[Long]("c")
    }
  }

  def findWithConn(id: Long)(implicit conn: Connection): Option[ML] = {

    SQL(
      """
        SELECT * FROM ml_proposal
          WHERE id = {id}
          AND status = {status}
      """
    ).on(
      'id     -> id,
      'status -> MLProposalStatus.Accepted.toString
    ).apply.headOption map { row =>
      val id = row[Long]("id")
      lazy val lastMailedAt = this.lastMailedAt(id)
      ML(
        id,
        row[String]("ml_title"),
        MLArchiveType.withName(row[String]("archive_type")),
        new URL(row[String]("archive_url")),
        new DateTime(row[Date]("judged_at")),
        lastMailedAt)
    }
  }

  def find(ids: List[Long]): List[ML] =
    DB.withConnection { implicit conn =>
      findWithConn(ids)
    }

  private[models] def findWithConn(ids: List[Long])(implicit conn: Connection): List[ML] = {

    if (ids.length == 0) return Nil

    SQL(s"""
      SELECT * FROM ml_proposal
        WHERE status = {status}
        AND id IN (%s)
    """ format ids.mkString(", ")
    ).on(
      'status -> MLProposalStatus.Accepted.toString
    ).apply() map { row =>
      val id = row[Long]("id")
      lazy val lastMailedAt = this.lastMailedAt(id)
      ML(
        id,
        row[String]("ml_title"),
        MLArchiveType.withName(row[String]("archive_type")),
        new URL(row[String]("archive_url")),
        new DateTime(row[Date]("judged_at")),
        lastMailedAt)
    } toList
  }

  /** Get the latest mailed datetime at the id's ML. */
  def lastMailedAt(id: Long): Option[DateTime] = {
    // TODO find by search engine.
    Some(DateTime.now)
  }
}