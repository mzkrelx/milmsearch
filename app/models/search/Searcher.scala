package models.search
import java.net.URL
import org.elasticsearch.action.search.SearchRequestBuilder
import org.elasticsearch.index.query._
import org.elasticsearch.search.SearchHit
import org.elasticsearch.search.sort.SortOrder
import org.joda.time.DateTime
import javax.mail.internet.InternetAddress
import models._
import org.apache.lucene.search.TermQuery

case class SearchException(msg: String) extends Exception(msg)

object Searcher {

  def search(req: SearchRequest): MailSearchResult = {
    try {
      val mailPage = createMailPage(req)
      val options = createOptions(req)
      MailSearchResult(mailPage, options._1, options._2)
    } catch {
      case e: Exception => throw SearchException(e.getMessage)
    }
  }

  private def createMailPage(req: SearchRequest): Page[Mail] = {
    val response = searchByKeyword(req, makeQuery(req.fields, req.keywords))
    val hits = response.getHits.getHits.toList

    Page[Mail](
      hits map ( doc =>
        Mail(
          DateTime.parse(doc.getSource.get("date").toString),
          new InternetAddress(doc.getSource.get("fromAddr").toString),
          doc.getSource.get("subject").toString,
          doc.getSource.get("body").toString.slice(0, 300), // TODO highlight
          new URL(doc.getSource.get("srcURL").toString),
          doc.getSource.get("MLTitle").toString,
          new URL(findMLs(hits).find(_.id.toString == doc.getSource.get("MLID").toString)
            .get.archiveURL.toString)
        )
      ),
      response.getHits.getTotalHits, req.startIndex, req.itemsPerPage
    )
  }

  private def searchByKeyword(req: SearchRequest, query: QueryBuilder) = {
    val searchRequest  = new SearchRequestBuilder(ElasticSearch.client)
      .setIndices("milmsearch")
      .setTypes("mailInfo")
      .setQuery(query)
      .setFilter(makeFilter(req))
      .addSort(extractField(req.order), extractOrder(req.order))
      .setFrom(req.startIndex.toInt)
      .setSize(req.itemsPerPage)

    searchRequest.execute.actionGet
  }

  private def makeQuery(fields: Set[MailSearchField.Value], keywords: String): MultiMatchQueryBuilder = {
    val fieldsSeq = fields.map( field =>
      field match {
        case MailSearchField.Body => "body"
        case MailSearchField.From => "fromAddr"
        case MailSearchField.Subject => "subject"
        case MailSearchField.MLTitle => "MLTitle"
      }
    ).toSeq

    QueryBuilders.multiMatchQuery(keywords, fieldsSeq: _*)
  }

  private def makeFilter(req: SearchRequest) = {
    val rangeFilter = makeRangeFilter(req)

    val mlIDFilter = makeMLFilter(req)

    val fromAddrFilter = makeFromFilter(req)

    val filters = List(rangeFilter, mlIDFilter, fromAddrFilter) collect {
      case Some(f) => f
    }

    FilterBuilders.andFilter(filters: _*)
  }

  private def makeRangeFilter(req: SearchRequest): Option[RangeFilterBuilder] = {
    Some(FilterBuilders.rangeFilter("date").from(req.fromDate).to(req.toDate))
  }

  private def makeMLFilter(req: SearchRequest): Option[OrFilterBuilder] = {
    req.mlIDs.map(_.toString) toSeq match {
      case x if x nonEmpty => Some(FilterBuilders.orFilter(
        FilterBuilders.termsFilter("MLID", x: _*)))
      case _ => None
    }
  }

  private def makeFromFilter(req: SearchRequest): Option[OrFilterBuilder] = {
    req.froms match {
      case froms if froms nonEmpty => Some(FilterBuilders.orFilter(
        FilterBuilders.queryFilter(QueryBuilders.fieldQuery(
          "fromAddr",
          froms.map(_.email.toString).mkString(",")
        ))
      ))
      case _ => None
    }
  }

  private def extractField(order: MailSearchOrder.Value): String = {
    order match {
      case MailSearchOrder.DateAsc  => "date"
      case MailSearchOrder.DateDesc => "date"
    }
  }

  private def extractOrder(order: MailSearchOrder.Value): SortOrder = {
    order match {
      case MailSearchOrder.DateAsc  => SortOrder.ASC
      case MailSearchOrder.DateDesc => SortOrder.DESC
    }
  }

  private def createOptions(req: SearchRequest): (List[MLOption], List[FromOption]) = {
    val searchRequest  = new SearchRequestBuilder(ElasticSearch.client)
      .setIndices("milmsearch")
      .setTypes("mailInfo")
      .setQuery(makeQuery(req.fields, req.keywords))
      .setFilter(makeRangeFilter(req).get)
      .addSort(extractField(req.order), extractOrder(req.order))
    val hits = searchRequest.execute.actionGet.getHits.getHits.toList

    (makeMLOptions(hits), makeFromOptions(hits))
  }

  private def makeMLOptions(hits: List[SearchHit]): List[MLOption] = {
    findMLs(hits).map { ml =>
        MLOption(ml.id, ml.mlTitle)
    } distinct
  }

  private def makeFromOptions(hits: List[SearchHit]): List[FromOption] = {
    hits map { doc =>
      FromOption(
        new InternetAddress(doc.getSource.get("fromAddr").toString).getPersonal,  // TODO 名前とアドレス別々にインデクシング(#85)を修正後、InternetAddress介さないように直す
        MailAddress(new InternetAddress(doc.getSource.get("fromAddr").toString).getAddress))
    } distinct
  }

  private def findMLs(hits: List[SearchHit]): List[ML] = {
    val mlIDs = hits.map(_.getSource.get("MLID").toString.toLong).toList
    ML.find(mlIDs)
  }

  def searchLastMail(mlID: Long): Option[Mail] = {
    val response  = new SearchRequestBuilder(ElasticSearch.client)
      .setIndices("milmsearch")
      .setTypes("mailInfo")
      .setQuery(QueryBuilders.termQuery("MLID", mlID))
      .addSort("date", SortOrder.DESC)
      .setFrom(0).setSize(1)
      .execute
      .actionGet

    val hits = response.getHits.getHits.toList

    hits map { doc =>
      Mail(
        DateTime.parse(doc.getSource.get("date").toString),
        new InternetAddress(doc.getSource.get("fromAddr").toString),
        doc.getSource.get("subject").toString,
        doc.getSource.get("body").toString.slice(0, 300), // TODO highlight
        new URL(doc.getSource.get("srcURL").toString),
        doc.getSource.get("MLTitle").toString,
        new URL(findMLs(hits).find(_.id.toString == doc.getSource.get("MLID").toString)
          .get.archiveURL.toString)
      )
    } headOption
  }
}
