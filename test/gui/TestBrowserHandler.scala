package test.gui

import play.api.test.TestBrowser

class TestBrowserHandler(browser: TestBrowser) {

  def findTableRows:java.util.List[String] = {
    browser.$("table tbody tr").getTexts
  }
  
  def findPageTitle:String = {
    browser.$("h1").getText
  }
}
