package controllers

import org.joda.time.DateTime
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import models.search.SearchRequest
import models.search.MailSearchField
import org.joda.time.format.DateTimeFormat
import models.search.Searcher

object Application extends Controller {

  val searchForm = Form(
    mapping(
      "keywords"   -> nonEmptyText,
      "fromDate"   -> jodaDate(Defaults.searchDateFormat),
      "toDate"     -> jodaDate(Defaults.searchDateFormat),
      "fields"     -> list(text)
    ){
      (keywords, fromDate, toDate, fields) =>
        SearchRequest(keywords, fromDate, toDate,
          fields map { MailSearchField.withName(_) } toSet)
    }{
      (search: SearchRequest) => Some((
        search.keywords,
        search.fromDate,
        search.toDate,
        search.fields map { _.toString } toList))
    }
  )

  def index = Action {
    Ok(views.html.index())
  }

  def inquiry = Action {
    Ok(views.html.inquiry())
  }

  def rule = Action {
    Ok(views.html.rule())
  }

  def poricy = Action {
    Ok(views.html.poricy())
  }

  def help = Action {
    Ok(views.html.help())
  }

  def search() = Action { implicit request =>
    searchForm.bindFromRequest.fold(
      errorForm => BadRequest(views.html.index()),
      searchRequest => {
        Logger.debug("Search => " + searchRequest)
        val result = Searcher.search(searchRequest)

        Ok(views.html.searchResult(searchRequest, result))
      }
    )
  }

}