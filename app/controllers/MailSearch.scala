package controllers

import models.search._
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import javax.mail.internet.InternetAddress

object MailSearch extends Controller {

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

  def search() = Action { implicit request =>
    searchForm.bindFromRequest.fold(
      errorForm => BadRequest(views.html.index()),
      searchRequest => {
        Logger.debug("Search => " + searchRequest)
        val result = Searcher.search(searchRequest)

        Ok(views.html.searchResult(searchRequest, result,
          List(MLOption(1, "helo1"), MLOption(2, "helo2")),
          List(FromOption(new InternetAddress("email@sample.com", "SAMPLE")),
              FromOption(new InternetAddress("email@sample.com", "SAMPLE"))
          )
        ))
      }
    )
  }
}
