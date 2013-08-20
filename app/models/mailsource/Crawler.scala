package models.mailsource

import java.net.URL
import java.sql.Connection
import scala.util.Try
import models.ML
import models.MLArchiveType
import models.MLArchiveType._
import models.mailsource.crawlers._
import play.api.UnexpectedException

class CrawlingException(msg: String) extends Exception(msg)

object Crawler {

  def crawlingTest(archiveType: MLArchiveType.Value, archiveURL: URL): Try[Mail] = {

    archiveType match {
      case Mailman => MailmanCrawler.crawlingTest(archiveURL)
      case SourceForgeJP => SourceForgeJPCrawler.crawlingTest(archiveURL)
      case irregular => throw new CrawlingException(s"irregular archive type.[${irregular}]")
    }
  }

  def crawlingWithConn(mlID: Long)(implicit conn: Connection) {

    val ml = ML.findWithConn(mlID).getOrElse(throw new NoSuchElementException("ml to crawl not found."))

    ml.archiveType match {
      case Mailman => MailmanCrawler.crawling(ml)
      case SourceForgeJP => SourceForgeJPCrawler.crawling(ml)
      case irregular => throw new CrawlingException(s"irregular archive type.[${irregular}]")
    }
  }

}