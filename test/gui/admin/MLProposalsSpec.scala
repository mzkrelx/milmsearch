package test.gui.admin

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import models._
import models.MLProposalStatus._
import java.net.URL
import org.joda.time.DateTime
import java.util.Date
import anorm.Pk
import anorm.NotAssigned
import org.specs2.specification.BeforeExample
import org.specs2.specification.AfterExample
import org.openqa.selenium.WebDriver

case class MLProposalTestData(
  number: Int,
  status: String,
  createdAt: DateTime)

class MLProposalsSpec extends Specification with BeforeExample with AfterExample {

  def before = {
    println("before")
  }

  def after = {
    println("after")
  }

  def createMLProposal(data: MLProposalTestData) {

    val model = MLProposal(
      NotAssigned,
      "申込者",
      "test@mail.com",
      "test ml %s %d".format(data.status, data.number),
      MLProposalStatus.withName(data.status),
      MLArchiveType.withName("mailman"),
      new URL("https://sheep.milmsearch.org/teirei-kun"),
      "testdesu",
      Option(new DateTime(2011, 10, 10, 3, 50, 0)),
      data.createdAt,
      new DateTime(2011, 10, 23, 3, 50, 0))

    MLProposal.create(model)
  }

  def createMLProposalList(status: String) {
    for (i <- 1 to 10) {
      for (j <- 1 to 10) {
        val createdAd = new DateTime(2011, i, j, 3, 50, 0)
        val data = MLProposalTestData(i * j, status, createdAd)
        createMLProposal(data)
      }
    }
  }
  
  "mlProposals" should {

    "status is new" in {
      running(TestServer(3333), HTMLUNIT) { browser =>

        createMLProposalList("new")

        val browserHandler = new ProposalsBrowserHandler(browser)

        browserHandler.gotoProposalListPage("new")
        browserHandler.findMLList.size mustEqual (10)

        browserHandler.findMLDataAtRowIndex(0) mustEqual ("2011/01/01test ml new 1")
        browserHandler.findMLDataAtRowIndex(3) mustEqual ("2011/01/04test ml new 4")

        browserHandler.findPageTitle mustEqual ("審査待ちML一覧")

        browserHandler.gotoProposalListPage("accepted")
        browserHandler.findMLList.size mustEqual (1)
      }
    }

    "status is accepted" in {
      running(TestServer(3333), HTMLUNIT) { browser =>
      	
        createMLProposalList("accepted")
      	
        val browserHandler = new ProposalsBrowserHandler(browser)
        
        browserHandler.gotoProposalListPage("accepted");
        browserHandler.findMLList.size mustEqual(10)
        
        browserHandler.gotoProposalListPage("new")		
        browserHandler.findMLList.size mustEqual(1)
        
      }
    }

    "status is rejected" in {
      running(TestServer(3333), HTMLUNIT) { browser =>

        createMLProposalList("rejected")
        
	    val browserHandler = new ProposalsBrowserHandler(browser)
      	
        browserHandler.gotoProposalListPage("rejected");
        browserHandler.findMLList.size mustEqual(10)
        
        browserHandler.gotoProposalListPage("new")		
        browserHandler.findMLList.size mustEqual(1)
      }
    }

  }
}