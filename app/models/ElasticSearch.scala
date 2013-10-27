package models

import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.transport.InetSocketTransportAddress
import utils.Utils.playConfig
import sun.security.pkcs11.ConfigurationException
import utils.ConfigException

object ElasticSearch {
  lazy val hostname = playConfig.getString("elasticsearch.hostName").getOrElse(throw ConfigException("elasticsearch hostname error"))
  lazy val port = playConfig.getInt("elasticsearch.port").getOrElse(throw ConfigException("elasticsearch port error"))
  val client = new TransportClient().addTransportAddress(new InetSocketTransportAddress(hostname, port))
}
