package models.mailsource

import org.elasticsearch.common.xcontent.XContentFactory._
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.transport.InetSocketTransportAddress
import utils.Utils.playConfig
import models.ML
import models.ElasticSearch

case class IndexingException(msg: String) extends Exception(msg)
object Indexer {
  def indexing(ml:ML, mail:Mail) {

    ElasticSearch.client.prepareIndex("milmsearch", "mailInfo")
      .setSource(jsonBuilder().startObject()
        .field("date", mail.date.toString())
        .field("fromAddr", mail.fromAddr.toString())
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
}