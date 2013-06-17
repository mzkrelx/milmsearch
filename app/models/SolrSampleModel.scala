
package models
import com.github.seratch.scalikesolr._
import java.net.URL
import com.github.seratch.scalikesolr.request.query.Query

case class SolrSampleModels()

object SolrSampleModels {

  def select(data: String): QueryResponse = {
    val client = Solr.httpServer(new URL("http://localhost:8983/solr")).newClient
    val request = QueryRequest(query = Query(data))
    client.doQuery(request)
  }

  def update(jsonData: String) {
    val client = Solr.httpServer(new URL("http://localhost:8983/solr")).newClient()
    val request = new UpdateRequest(requestBody = "[" + jsonData + "]".stripMargin)
    val response = client.doUpdateInJSON(request)
    client.doCommit()
  }

  def delete(deleteId: String) {
    val client = Solr.httpServer(new URL("http://localhost:8983/solr")).newClient()
    val request = new DeleteRequest(uniqueKeysToDelete = List(deleteId))
    val response = client.doDeleteDocuments(request)
    client.doCommit(new UpdateRequest())
  }
}