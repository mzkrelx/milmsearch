package models

import models.mailsource.Mail
import org.elasticsearch.node.NodeBuilder._
import org.elasticsearch.common.xcontent.XContentFactory._
import org.elasticsearch.action.get.GetResponse
import org.elasticsearch.action.search.SearchRequestBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.transport.InetSocketTransportAddress
import utils.Utils.playConfig

case class IndexingException(msg: String) extends Exception(msg)
object Indexer {
  def indexing(ml:ML, mail:Mail) {
    
    val hostname = playConfig.getString("elasticsearch.hostName").getOrElse(throw IndexingException("elasticsearch hostname error"))
    val port = playConfig.getInt("elasticsearch.port").getOrElse(throw IndexingException("elasticsearch port error"))
      val client = new TransportClient()
      .addTransportAddress(new InetSocketTransportAddress(hostname, port))
	    client
	      .prepareIndex("milmsearch", "mailInfo")
	      .setSource(
    		  jsonBuilder()
      		      .startObject()
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