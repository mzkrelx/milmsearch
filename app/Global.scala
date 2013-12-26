import models.executors.FirstCrawlingExecutor
import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import scala.concurrent.Future

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    Logger.info("MilmSearch has started.")
  }

  override def onStop(app: Application) {
    FirstCrawlingExecutor.es.shutdown
    Logger.info("First Crawling Executor shutdown.")

    Logger.info("MilmSearch shutdown...")
  }

  override def onHandlerNotFound(request: RequestHeader): Future[SimpleResult] = {
    Future.successful(NotFound(views.html.notFound()))
  }

  override def onBadRequest(request: RequestHeader, error: String) = {
    Future.successful(BadRequest("Bad Request: " + error))
  }

  override def onError(request: RequestHeader, ex: Throwable) = {
    Future.successful(InternalServerError(views.html.error()))
  }
}
