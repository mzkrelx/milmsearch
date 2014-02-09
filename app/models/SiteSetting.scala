package models

import java.sql.Connection
import anorm._
import play.api.Play.current
import play.api.db.DB

case class SiteSetting(footerHtml: String)

object SiteSetting {
  def update(siteSetting: SiteSetting) {
    DB.withConnection { implicit conn =>
      SQL(s"""
        UPDATE site_setting SET
          footer_html = {footer_html}""")
        .on(
          'footer_html    -> siteSetting.footerHtml)
        .executeUpdate()
    }
  }

  def find: SiteSetting =
    DB.withConnection { implicit conn =>
      findWithConn(conn)
    }

  private[models] def findWithConn(implicit conn: Connection): SiteSetting = {
    SQL("SELECT * FROM site_setting WHERE id = 1").apply.map { row =>
      SiteSetting(row[String]("footer_html"))
    }.toList.head
  }

}