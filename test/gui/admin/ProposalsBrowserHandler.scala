package test.gui.admin

import test.gui.TestBrowserHandler
import play.api.test.TestBrowser

class ProposalsBrowserHandler(browser: TestBrowser) extends TestBrowserHandler(browser: TestBrowser) {

  def gotoProposalListPage(status: String) {
    browser.goTo("http://localhost:3333/admin/ml-proposals?status=%s".format(status))
  }

  def findMLList:java.util.List[String] = {
    findTableRows
  }

  def findMLDataAtRowIndex(number: Int):String = {
    findTableRows.get(number)
  }

}