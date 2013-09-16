package models.archive

import akka.actor.{ActorSystem, Actor, Props}
import models.ML
import java.net.URL
import models.mailsource.Mail
import models.MLArchiveType
import org.joda.time.DateTime
import models.mailsource.Mail

case class ArchiveRequest(arcType: MLArchiveType.Value, arcURL: URL)
case class PageCrawlingRequest(pageURL: URL)
case class PageCrawlingResult(pageURL: URL, mail: Mail)
case class MailIndexingRequest(mail: Mail)
case class MailIndexingCompleted(mail: Mail)
case class ArchiveResult(
  startDate: DateTime,
  endDate: DateTime,
  lastArchivedMail: Mail,
  totalArchivedMails: Int)

class Archiver extends Actor {
  val system = ActorSystem("archive")
  val MaxCrawlingWorkers = 3
  val MaxIndexingWorkers = 3
  val crawlingWorkers = (1 to MaxCrawlingWorkers) map { i =>
    system.actorOf(Props[CrawlingWorker], s"CrawlingWorker-$i")
  }
  val indexingWorkers = (1 to MaxIndexingWorkers) map { i =>
    system.actorOf(Props[IndexingWorker], s"IndexingWorker-$i")
  }
  
  def receive = {
    case ArchiveRequest(arcType, arcURL) => {
      val pageURLs = Seq(new URL("http://dummy/"))  // TODO split crawling page
      pageURLs.zipWithIndex foreach { case (pageURL, idx) =>
        crawlingWorkers(idx % crawlingWorkers.length) ! PageCrawlingRequest(pageURL)
      }
    }
       
    case PageCrawlingResult(pageURL, mail) =>
      indexingWorkers.head ! MailIndexingRequest(mail)  // TODO task balancing
      
    case MailIndexingCompleted(mail) =>
      sender ! ArchiveResult(DateTime.now, DateTime.now, mail, 1)
  }
}

class CrawlingWorker extends Actor {
  def receive = {
    case PageCrawlingRequest(pageURL) =>
      sender ! PageCrawlingResult(pageURL, null) // TODO
  }
}

class IndexingWorker extends Actor {
  def receive = {
    case MailIndexingRequest(mail) =>
      sender ! MailIndexingCompleted(mail)
  }
}