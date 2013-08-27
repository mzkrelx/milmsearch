import java.util.concurrent.Executors
import play.api._
import models.executors.FirstCrawlingExecutor

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    Logger.info("MilmSearch has started.")
  }

  override def onStop(app: Application) {
    FirstCrawlingExecutor.es.shutdown
    Logger.info("First Crawling Executor shutdown.")

    Logger.info("MilmSearch shutdown...")
  }

}
