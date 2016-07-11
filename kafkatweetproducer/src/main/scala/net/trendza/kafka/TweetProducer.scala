package net.trendza.kafka


import java.util
import java.util.Properties
import java.util.concurrent.{Future, TimeUnit}

import akka.actor.Actor.Receive
import com.typesafe.config.{Config, ConfigFactory}
import akka.actor.{Actor, ActorLogging}
import kafka.serializer.{DefaultEncoder, StringEncoder}
import net.trendza.tweets.domain.Tweet
import org.apache.kafka.clients.producer._
import org.apache.kafka.common.serialization.Serializer
import org.apache.kafka.common.{Metric, MetricName, PartitionInfo}
/**
  * Created by mark on 10/07/16.
  */

trait TweetSender {
  def send(t: Tweet): Unit
}

trait KafkaTweetSender extends TweetSender{

  val config = ConfigFactory.load()
  val producerConfig = {
    val cfg = config.getConfig("kafka")
    val props = new Properties()
    props put("zookeeper.host", cfg.getString("zookeeper.host"))
    props put("bootstrap.servers", cfg.getString("bootstrap.servers"))
    props.put("serializer.class", "kafka.serializer.StringEncoder");
    props put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props put("value.serializer", classOf[TweetSerializer])
    props put ("request.required.acks", Int.box(cfg.getInt("request.required.acks")))
    props put("producer.type", Int.box(cfg.getInt("producer.type")))
    props put("serializer.class", Class.forName(cfg.getString("serializer.class")))
    props
  }
  private val tweetProducer = new KafkaProducer[String,Tweet](producerConfig, null, null)

  override def send(t: Tweet): Unit = tweetProducer.send(new ProducerRecord("tweets", t.id toString, t))
}

class TweetProducerActor extends Actor with ActorLogging {
  me: TweetSender =>

  override def receive: Receive = {
    case t:Tweet=> me.send(t)
    case _ =>
  }

}


class TweetSerializer extends Serializer[Tweet] {

  import net.trendza.tweets.core.TweetJsonProtocol._

  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = ()

  override def serialize(topic: String, data: Tweet): Array[Byte] = TweetFormat.write(data).toString().getBytes

  override def close(): Unit = ()
}