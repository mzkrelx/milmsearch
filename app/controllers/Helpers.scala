package controllers

import play.api._
import play.api.mvc._
import utils.BadRequestException

/**
 * try-catch 4xx error, then send 4xx response with message
 */
case class TryCatch4xx[A](action: Action[A]) extends Action[A] {
  
  def apply(request: Request[A]): Result = {
    try {
      action(request)
    } catch {
      case e: BadRequestException => {
        Logger.warn(e.msg)
        Play.maybeApplication map {
          _.global.onBadRequest(request, e.msg)
        } getOrElse Results.BadRequest
      }
      case e: Throwable => throw e
    } 
  }
  
  lazy val parser = action.parser
}

/**
 * Helper for pagination
 */
case class Page[A](
    items: Seq[A],
    totalResults: Long,
    startIndex: Long = 0,
    itemsPerPage: Int = 10) {
  
  lazy val prevIndex  = Option(startIndex - itemsPerPage).filter(_ >= 0)
  lazy val nextIndex  = Option(startIndex + itemsPerPage).filter(_ < totalResults)
  lazy val totalPages = (totalResults / itemsPerPage) +
                          ((totalResults % itemsPerPage) min 1) 
}