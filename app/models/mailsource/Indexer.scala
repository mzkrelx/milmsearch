package models.mailsource
import org.elasticsearch.action.search.SearchRequestBuilder

import org.elasticsearch.common.xcontent.XContentFactory._
import org.elasticsearch.index.query.QueryBuilders

import models.ElasticSearch
import models.ML
import play.api.Logger

case class IndexingException(msg: String) extends Exception(msg)

object Indexer {

  def indexing(ml:ML, mail:Mail) {
    if (!isIndexed(mail.srcURL.toString)) {
      executeIndex(ml, mail)
    } else {
      Logger.warn(s"Already indexed.[$mail.srcURL]")
    }
  }

  private def isIndexed(srcURL: String) = {
    val hits = new SearchRequestBuilder(ElasticSearch.client)
      .setIndices("milmsearch")
      .setTypes("mailInfo")
      .setQuery(QueryBuilders.queryString("\""+srcURL+"\""))
      .setFrom(0).setSize(1)
      .execute
      .actionGet
      .getHits.getHits.toList

    hits headOption match {
      case Some(doc) => true
      case None      => false
    }
  }

  private def executeIndex(ml: ML, mail: Mail) =
    ElasticSearch.client.prepareIndex("milmsearch", "mailInfo")
      .setSource(jsonBuilder().startObject()
        .field("date", mail.date.toString())
        .field("fromAddr", mail.fromAddr.getAddress().toString())
        .field("fromPersonal", mail.fromAddr.getPersonal().toString())
        .field("subject", mail.subject)
        .field("body", mail.body)
        .field("srcURL", mail.srcURL.toString())
        .field("MLTitle", ml.mlTitle)
        .field("MLID", ml.id.toString())
        .endObject())
      .setOperationThreaded(false)
      .execute()
      .actionGet()

}