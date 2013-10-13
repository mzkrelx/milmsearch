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
import play.api.Logger
import org.elasticsearch.action.search.SearchPhaseExecutionException
import org.elasticsearch.search.highlight.HighlightField
import scala.xml.Utility

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
    val mls = findMLs(hits)
    Page[Mail](
      hits map { toMail(_, mls) },
      response.getHits.getTotalHits, req.startIndex, req.itemsPerPage
    )
  }

  private def searchByKeyword(req: SearchRequest, query: QueryBuilder) = {
    val searchRequest = new SearchRequestBuilder(ElasticSearch.client)
      .setIndices("milmsearch")
      .setTypes("mailInfo")
      .setQuery(query)
      .setFilter(makeFilter(req))
      .addSort(extractField(req.order), extractOrder(req.order))
      .addHighlightedField("body", 0, 0)
      .addHighlightedField("subject", 0, 0)
      .setFrom(req.startIndex.toInt)
      .setSize(req.itemsPerPage)

    searchRequest.execute.actionGet
  }

  private def makeQuery(fields: Set[MailSearchField.Value], keywords: String): MultiMatchQueryBuilder = {
    def r(fieldsSeq: List[String], fields: List[MailSearchField.Value]): List[String] = {
      import MailSearchField._
      fields match {
        case Nil => fieldsSeq
        case Body :: xs => r("body" :: fieldsSeq, xs)
        case From :: xs => r("fromPersonal" :: "fromAddr" :: fieldsSeq, xs)
        case Subject :: xs => r("subject" :: fieldsSeq, xs)
        case MLTitle :: xs => r("MLTitle" :: fieldsSeq, xs)
        case other :: xs => throw SearchException(s"Illegal Field Error[$other]")
      }
    }
    QueryBuilders.multiMatchQuery(keywords, r(Nil, fields.toList): _*)
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
          froms.map(_.email.toString).mkString(",")))))
      case _ => None
    }
  }

  private def extractField(order: MailSearchOrder.Value): String = {
    order match {
      case MailSearchOrder.DateAsc => "date"
      case MailSearchOrder.DateDesc => "date"
    }
  }

  private def extractOrder(order: MailSearchOrder.Value): SortOrder = {
    order match {
      case MailSearchOrder.DateAsc => SortOrder.ASC
      case MailSearchOrder.DateDesc => SortOrder.DESC
    }
  }

  private def createOptions(req: SearchRequest): (List[MLOption], List[FromOption]) = {
    val searchRequest = new SearchRequestBuilder(ElasticSearch.client)
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
        doc.getSource.get("fromPersonal").toString,
        MailAddress(doc.getSource.get("fromAddr").toString))
    } distinct
  }

  private def findMLs(hits: List[SearchHit]): List[ML] = {
    val mlIDs = hits.map(_.getSource.get("MLID").toString.toLong).toList
    ML.find(mlIDs)
  }

  private def toMail(doc: SearchHit, mls: List[ML]) = {
    val subject = doc.highlightFields.get("subject") match {
      case f: HighlightField => {
        val subject = f.fragments.apply(0).toString
        Utility.escape(subject).replaceAll(Utility.escape("<em>"), "<em>")
          .replaceAll(Utility.escape("</em>"), "</em>")
      }
      case null => doc.getSource.get("subject").toString
    }
    val body = doc.highlightFields.get("body") match {
      case f: HighlightField => {
        val highlightBody = f.fragments.apply(0).toString
        val bodyBeginPosition = highlightBody.indexOf("<em>") - 5 max 0
        val bodyEndPosition = highlightBody.indexOf("</em>") + 5 +
          ((highlightBody.length * 0.1).toInt min 150)
        val sliced = highlightBody.slice(bodyBeginPosition, bodyEndPosition)
        Utility.escape(sliced).replaceAll(Utility.escape("<em>"), "<em>")
          .replaceAll(Utility.escape("</em>"), "</em>")
      }
      case null => {
        val normalBody = doc.getSource.get("body").toString
        val bodyBeginPosition = 0
        val bodyEndPosition = (normalBody.length * 0.1).toInt min 150
        normalBody.slice(bodyBeginPosition, bodyEndPosition)
      }
    }

    Mail(
      DateTime.parse(doc.getSource.get("date").toString),
      new InternetAddress(doc.getSource.get("fromAddr").toString,
        doc.getSource.get("fromPersonal").toString),
      subject,
      body,
      new URL(doc.getSource.get("srcURL").toString),
      doc.getSource.get("MLTitle").toString,
      new URL(mls.find(_.id.toString == doc.getSource.get("MLID").toString)
        .get.archiveURL.toString)
    )
  }

  def searchLastMail(mlID: Long): Option[Mail] = {
    val response  = try {
      new SearchRequestBuilder(ElasticSearch.client)
        .setIndices("milmsearch")
        .setTypes("mailInfo")
        .setQuery(QueryBuilders.termQuery("MLID", mlID))
        .addSort("date", SortOrder.DESC)
        .setFrom(0).setSize(1)
        .execute
        .actionGet
    } catch {
      case e: SearchPhaseExecutionException => {
        Logger.error("Failed to search last mail. Return None.", e)
        return None
      }
    }
    val hits = response.getHits.getHits.toList
    Logger.debug(hits.toString)
    hits match { 
      case Nil => None
      case _ => Some(toMail(hits.last, findMLs(hits)))
    }
  }
}
