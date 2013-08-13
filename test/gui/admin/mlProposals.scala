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
  createdAt: DateTime  
)

/**
 * add your integration spec here.
 * An integration test will fire up a whole play application in a real (or headless) browser
 */
class IntegrationSpec extends Specification with BeforeExample with AfterExample {
  
   def before = {
      println("before"); 
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
		new DateTime(2011, 10, 23, 3, 50, 0)
		)
		
		MLProposal.create(model)
   }

   def createMLProposalList(status: String)
   {
     for (i <- 1 to 10) {
        	for (j <- 1 to 10) {
        	  val createdAd = new DateTime(2011, i, j, 3, 50, 0)
        		val data = MLProposalTestData(i * j, status, createdAd)
        		createMLProposal(data)
        	}
        }
   }
   
   def gotoProposalListPage(browser:TestBrowser, status:String) {
     browser.goTo("http://localhost:3333/admin/ml-proposals?status=%s".format(status))
   }
   
	def MlListWithBrowser(browser: TestBrowser) = {
	  browser.$("table tbody tr").getTexts
	}
	
	def MlDataWithBrowserAndRowIndex(browser: TestBrowser, number:Int) = {
	  MlListWithBrowser(browser).get(number)
	}
   
  "mlProposals" should {
    
    "status is new"  in {
      running(TestServer(3333), HTMLUNIT) { browser =>
      
        createMLProposalList("new")
        
        gotoProposalListPage(browser, "new")
        MlListWithBrowser(browser).size mustEqual(10)
        
        MlDataWithBrowserAndRowIndex(browser, 0) mustEqual("2011/01/01test ml new 1")
        MlDataWithBrowserAndRowIndex(browser, 3) mustEqual("2011/01/04test ml new 4")
        
        gotoProposalListPage(browser, "accepted")
        MlListWithBrowser(browser).size mustEqual(1)
      }
    }
    
    "status is accepted" in {
      running(TestServer(3333), HTMLUNIT) { browser =>
      	
        createMLProposalList("accepted")
      	
        gotoProposalListPage(browser, "accepted");
        MlListWithBrowser(browser).size mustEqual(10)
        
        gotoProposalListPage(browser, "new")		
        MlListWithBrowser(browser).size mustEqual(1)
        
      }
    }
    
    "status is rejected" in {
      running(TestServer(3333), HTMLUNIT) { browser =>

        createMLProposalList("rejected")
      	
        gotoProposalListPage(browser, "rejected");
        MlListWithBrowser(browser).size mustEqual(10)
        
        gotoProposalListPage(browser, "new")		
        MlListWithBrowser(browser).size mustEqual(1)
      }
    }
  }
  
}