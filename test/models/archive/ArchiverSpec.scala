package models.archive

import java.net.URL
import akka.actor.Props
import utils.TestKitSpec
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.WordSpecLike
import org.scalatest.GivenWhenThen

class ArchiverSpec extends TestKitSpec("ArchiverSpec")
    with WordSpecLike
    with ShouldMatchers
    with GivenWhenThen {
  
  trait WorkerTest {
    lazy val crawler = system.actorOf(Props[CrawlingWorker])
    lazy val indexer = system.actorOf(Props[IndexingWorker])
  }
  
  "Archiver" should {
    "start archive if requested" in pending
  }
  
  "CrawlingWorker" should {
    "crawl page and report parsed mail if requested" in new WorkerTest {
      val pageURL = new URL("http://example.com/")
      Given(s"archive page -> $pageURL")
      
      val req = PageCrawlingRequest(pageURL)
      When(s"received $req")
      crawler ! req
      
      Then("crawl page")
      // TODO
      
      val res = PageCrawlingResult(pageURL, null)
      And(s"report $res")
      expectMsg(res)
    }
  }
  
  "IndexingWorker" should {
    "index mail to db" in pending 
  }
}