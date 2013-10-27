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
