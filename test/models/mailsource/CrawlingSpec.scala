package models.mailsource

import java.net.URL

import org.specs2.Specification

class CrawlingSpecSuite extends Specification { def is = skipped // only when network is fine
  s2"""
    This is a specification to check the Crawling on "milm-search-public ML"

    Returns Success                                                     $e0

    The First Mail's
      date  is '2011年 8月 24日 (水) 23:35:45 JST'                        $e1
      name  is 'Mizuki_Yamanaka'                                        $e2
      email is 'charles@1-ideal.info'                                   $e3
      subject is '[milm-search-public:1] ML作りました。よろしくお願いします。' $e4
      text start with 'みずきです。\n\n'                                   $e5
      text end with '\n━┷┷┷┷┷┷━━━━━━━━━━━━━━━━━━━━━━'                   $e6
      url is 'http://sourceforge...../000000.html'                      $e7
  """

  val crawlingTry = MailmanCrawler.crawlingTest(new URL(
    "http://sourceforge.jp/projects/milm-search/lists/archive/public/"))

  def e0 = crawlingTry.isSuccess mustEqual(true)

  val firstMail: Mail = crawlingTry.get

  def e1 = firstMail.date.toString("yyyy年 M月 d日 (EEE) HH:mm:ss zzz") mustEqual("2011年 8月 24日 (水) 23:35:45 JST")
  def e2 = firstMail.fromAddr.getPersonal mustEqual("Mizuki_Yamanaka")
  def e3 = firstMail.fromAddr.getAddress mustEqual("charles@1-ideal.info")
  def e4 = firstMail.subject mustEqual("[milm-search-public:1] ML作りました。よろしくお願いします。")
  def e5 = firstMail.body must startWith("みずきです。\n\n")
  def e6 = firstMail.body must endWith("\n━┷┷┷┷┷┷━━━━━━━━━━━━━━━━━━━━━━")
  def e7 = firstMail.srcURL.toString mustEqual("http://sourceforge.jp/projects/milm-search/lists/archive/public/2011-August/000000.html")
}