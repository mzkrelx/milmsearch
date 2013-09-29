package models.search

import java.net.URL
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.joda.time.DateTime
import javax.mail.internet.InternetAddress
import models._
import models.mailsource.IndexingException
import utils.MatchableEnumeration
import utils.Utils.playConfig
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.action.search.SearchType
import org.elasticsearch.action.search.SearchRequestBuilder
import org.elasticsearch.index.query.QueryBuilders
import play.Logger
import org.elasticsearch.index.query.FilterBuilders
import org.elasticsearch.search.sort.SortBuilders
import org.elasticsearch.search.sort.SortOrder
import models.ML
import org.elasticsearch.index.query.QueryBuilder

case class SearchRequest(
  keywords:     String,
  fromDate:     DateTime,
  toDate:       DateTime,
  fields:       Set[MailSearchField.Value],
  mlIDs:        Set[Long],
  froms:        Set[FromOption],
  itemsPerPage: Integer,
  order:        MailSearchOrder.Value,
  page:         Long)

object MailSearchOrder extends MatchableEnumeration {
  type MailSearchOrder = Value
  val DateAsc  = Value("date_asc")
  val DateDesc = Value("date_desc")
}

case class SearchResult(
  totalResultCount: Long,
  startIndex: Long,
  itemsPerPage: Long,
  items: List[Mail])

case class MLOption(id: Long, title: String)

case class FromOption(name: String, email: Email) {

  def value = s"""$name${FromOption.valueSeparator}${email.toView}"""

  def label = s"""$name <${email.toView}>"""

}

object FromOption {

  val valueSeparator = ", "
  lazy val valueSeparatorLength = valueSeparator.length

  def apply(ia: InternetAddress): FromOption = {
    this(ia.getPersonal, Email(ia.getAddress))
  }

  def apply(viewValue: String): FromOption = {
    val nameEmail = viewValue.splitAt(viewValue.lastIndexOf(valueSeparator))
    this(nameEmail._1, Email.fromView(nameEmail._2.substring(valueSeparatorLength)))
  }

}

case class Email(email: String) {
  def toView = email.replaceFirst("@", " ＠ ")
}

object Email {
  def fromView(viewEmail: String): Email =
    Email(viewEmail.replaceFirst(" ＠ ", "@"))
}

object Searcher {
  def search(req: SearchRequest): (Page[Mail], List[MLOption], List[FromOption]) = {

    val hostname = playConfig.getString("elasticsearch.hostName").getOrElse(throw IndexingException("elasticsearch hostname error"))
    val port = playConfig.getInt("elasticsearch.port").getOrElse(throw IndexingException("elasticsearch port error"))
    val client = new TransportClient().addTransportAddress(new InetSocketTransportAddress(hostname, port))
    
    // 複数のタイプからキーワードに合致するものを検索するためのクエリを定義
    val fieldsSeq = req.fields.map( field =>
        field match {
          case MailSearchField.Body => "body"
          case MailSearchField.From => "fromAddr"
          case MailSearchField.Subject => "subject"
          case MailSearchField.MLTitle => "MLTitle"
        }
    ).toSeq
    
    val queryBuilders = QueryBuilders.multiMatchQuery(req.keywords, fieldsSeq: _*)
    
    // フィルター定義
    val rangeFilter = Some(FilterBuilders.rangeFilter("date").from(req.fromDate).to(req.toDate))
    
    val mlIDFilter = req.mlIDs.map(id=>id.toString) toSeq match {
      case x if x nonEmpty => Some(FilterBuilders.orFilter(
        FilterBuilders.termsFilter("MLID", x: _*)))
      case _ => None
    }
    
    val fromAddrFilter = req.froms match {
      case x if x nonEmpty => Some(FilterBuilders.orFilter(
          FilterBuilders.queryFilter(QueryBuilders.fieldQuery(
              "fromAddr", x.map(from=>from.email.email.toString).toList.mkString(",")))))
      case _ => None
    }

    val filters = List(rangeFilter, mlIDFilter, fromAddrFilter) collect {
      case Some(f) => f 
    }

    val filter = FilterBuilders.andFilter(filters: _*)

    def order(order:MailSearchOrder.Value ): Pair[String, SortOrder] = {
      order match {
        case MailSearchOrder.DateAsc => ("date", SortOrder.ASC)
        case MailSearchOrder.DateDesc => ("date", SortOrder.DESC)
      }
    }

    val searchRequestBuilder  = new SearchRequestBuilder(client)
    .setIndices("milmsearch")   //複数指定するときはカンマ区切り(RDBで言うデータベース)
    .setTypes("mailInfo")       //複数指定するときはカンマ区切り(RDBで言うテーブル)
    .setQuery(queryBuilders)
    .setFilter(filter)
    .addSort(order(req.order)._1, order(req.order)._2)                //何個も並び替えするときはaddSort追加
    .setFrom(((req.itemsPerPage * req.page)-req.itemsPerPage).toInt)  //何番目から表示するか
    .setSize(req.itemsPerPage)                                        //何件表示するか

    // 検索実行
    val searchResponse = searchRequestBuilder.execute().actionGet();
    val hitItemsList = searchResponse.getHits().getHits().toList

    // MLIDからML情報を取得してる
    val mlList = ML.find( hitItemsList map ( doc =>
        doc.getSource().get("MLID").toString().toLong
      ) toList)

    val page = Page[Mail](
      hitItemsList map ( doc =>
        Mail(
          DateTime.parse(doc.getSource().get("date").toString()),
          new InternetAddress(doc.getSource().get("fromAddr").toString()),
          doc.getSource().get("subject").toString(),
          doc.getSource().get("body").toString().slice(0, 300),//先頭から300文字表示
          new URL(doc.getSource().get("srcURL").toString()),
          doc.getSource().get("MLTitle").toString(),
          new URL(mlList.find(_.id.toString == doc.getSource().get("MLID").toString()).get.archiveURL.toString())
          )
      ),
      totalResultCount = searchResponse.getHits().getTotalHits(),
      startIndex = req.page * req.itemsPerPage - req.itemsPerPage,
      itemsPerPage = req.itemsPerPage
    )

    // 「条件で絞り込む」用の検索
    val optionSearch  = new SearchRequestBuilder(client)
    .setIndices("milmsearch")
    .setTypes("mailInfo")
    .setQuery(queryBuilders)
    .setFilter(rangeFilter.get)
    .addSort(order(req.order)._1, order(req.order)._2)

    val optionHitsItemList = optionSearch.execute().actionGet().getHits().getHits().toList;

    val optionMlList =  ML.find( optionHitsItemList map ( doc =>
        doc.getSource().get("MLID").toString().toLong
      ) toList)

    val allmlOptionList = optionMlList.map(doc=>MLOption(doc.id, doc.mlTitle))
    val allFromOptionList = optionHitsItemList map ( doc =>
      FromOption(
          new InternetAddress(doc.getSource().get("fromAddr").toString()).getPersonal(),
          Email(new InternetAddress(doc.getSource().get("fromAddr").toString()).getAddress())))

    (page, allmlOptionList.distinct, allFromOptionList.distinct)
  }
}