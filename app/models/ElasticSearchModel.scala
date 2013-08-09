package models

import org.elasticsearch.node.NodeBuilder._
import org.elasticsearch.common.xcontent.XContentFactory._
import org.elasticsearch.action.get.GetResponse
import org.elasticsearch.action.search.SearchRequestBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.action.search.SearchResponse

object ElasticSearchModel {
  
    val node = nodeBuilder().client(true).node();
    val client = node.client()
  
  def update(jsonData: String, index: String){
    client
    .prepareIndex("milmsearch", "mailInfo", index)
    .setSource(jsonData)
    .setOperationThreaded(false)
    .execute()
    .actionGet()
  }
  
  def select(data: String) : SearchResponse = {
    val searchRequestBuilder  = new SearchRequestBuilder(client)
    .setIndices("milmsearch")
    .setTypes("mailInfo");

    searchRequestBuilder.setQuery(QueryBuilders.prefixQuery("_all", data));
    val searchResponse = searchRequestBuilder.execute().actionGet();
    searchResponse
  }
  
  def selectAll() : SearchResponse = {
    val searchRequestBuilder  = new SearchRequestBuilder(client)
    .setIndices("milmsearch")
    .setTypes("mailInfo");

    searchRequestBuilder.setQuery(QueryBuilders.matchAllQuery());
    val searchResponse = searchRequestBuilder.execute().actionGet();
    searchResponse
  }
  
  def delete(deleteId: String) {
    val response = client.prepareDelete("milmsearch", "mailInfo", deleteId)
        .execute()
        .actionGet();
  }

}