package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import org.elasticsearch.node.NodeBuilder._
import org.elasticsearch.common.xcontent.XContentFactory._
import models.ElasticSearchModel
import java.util.Date

object ElasticsearchSample extends Controller {
  val jsonForm = Form(tuple("jsonStr" -> text, "index"->text))
  val searchForm = Form("searchStr" -> text)
  
  def update = Action { implicit request =>
    val formData = jsonForm.bindFromRequest
    if(formData.hasErrors)
    {
      Ok(views.html.elasticsearchsample.index(ElasticSearchModel.selectAll, "update Failed"))
    }
    else
    {
      ElasticSearchModel.update(formData.get._1, formData.get._2)
      println(formData.get._1)
      Ok(views.html.elasticsearchsample.index(ElasticSearchModel.selectAll, "update Success"))
    }
  }
  
  def search = Action { implicit request =>
    val formData = searchForm.bindFromRequest
    if(formData.hasErrors)
    {
      Ok(views.html.elasticsearchsample.index(ElasticSearchModel.selectAll, "search Failed"))
    }
    else
    {
      Ok(views.html.elasticsearchsample.index(ElasticSearchModel.select(formData.get), "search Success"))
    }
  }
  
  def delete(deleteId: String) = Action {
    ElasticSearchModel.delete(deleteId)
    Ok(views.html.elasticsearchsample.index(ElasticSearchModel.selectAll, "deleted"))
  }
  
  def index = Action {
  val builder = jsonBuilder()
    .startObject()
        .field("title", "PHP_ML")
        .field("archive_type", "mailman")
        .field("archive_u_r_l", "http://aaa.com/archieve.html")
        .field("last_mailed_at",  "2013-06-21T03:04:05+09:00")
        .field("comment", "コメントテスト。こんにちは！")
    .endObject()
    ElasticSearchModel.update(builder.string, "1")
    Ok(views.html.elasticsearchsample.index(ElasticSearchModel.selectAll, "index"))
  }
}