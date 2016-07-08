package net.trendza.tweets.core

/**
  * Created by trantem on 05/07/2016.
  */


import java.io.{File, PrintWriter}
import java.util.Date

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.io.IO
import net.trendza.tweets.core.OAuth._
import net.trendza.tweets.core.TweetStreamActor.{OpenStream, StopStreaming}
import net.trendza.tweets.domain._
import spray.can.Http
import spray.client.pipelining._
import spray.http.{HttpRequest, MessageChunk, _}
import spray.json._

import scala.io.Source


object MyJsonProtocol extends DefaultJsonProtocol {
  implicit object DateFormater extends JsonFormat[Date]{
    override def write(obj: Date): JsValue = ???

    override def read(json: JsValue): Date = json match {
      case JsString(sval) => {
        val format = new java.text.SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy")
        format.parse(sval)
      }
    }
  }

  implicit val UserFormat = jsonFormat2(User)
  implicit val PlaceFormat = jsonFormat3(Place)
  implicit val PointFormat = jsonFormat1(Point)
  implicit val TweetFormat = jsonFormat7(Tweet)
}

trait TwitterAuthorization {
  def authorize: HttpRequest => HttpRequest
}

trait OAuthTwitterAuthorization extends TwitterAuthorization {
  val home = System.getProperty("user.home")
  val lines = Source.fromFile(s"$home/.twitter/trendza").getLines().toList

  val consumer = Consumer(lines(0), lines(1))
  val token = Token(lines(2), lines(3))

  val authorize: (HttpRequest) => HttpRequest = oAuthAuthorizer(consumer, token)
}


trait ChunkedTweetStreamer {
  this: ActorLogging =>

  private var chunk = ""

  def newTweet(t: Tweet): Unit

  def deserializeTweet(t: String): Either[DeserializationException,Tweet]

  def addChunk(entity: HttpEntity): Unit = {
    val entStr = entity.asString(defaultCharset = HttpCharsets.`ISO-8859-1`)
    if(entStr.endsWith("\r\n")){
      deserializeTweet(chunk + entStr) match {
        case Right(t) => newTweet(t)
        case Left(x) => log.error(s"Tweet deserialization error: $x")
      }
      chunk = ""
    }else{
      chunk = chunk + entStr
    }
  }
}

object TweetStreamActor {

  val twitterUri = Uri("https://stream.twitter.com/1.1/statuses/filter.json")
  case class OpenStream(follow: Array[String])
  case object StopStreaming
}

class TweetStreamerActor(uri: Uri, processor: ActorRef) extends Actor with ActorLogging with ChunkedTweetStreamer {

  this: TwitterAuthorization =>

  import MyJsonProtocol._

  val io = IO(Http)(context.system)

  def receive: Receive = ready

  def ready: Receive = {
    case OpenStream(follow) =>
      val body = HttpEntity(
        ContentType(MediaTypes.`application/x-www-form-urlencoded`),
        s"track=${follow mkString ","}")
      val rq =  HttpRequest(HttpMethods.POST, uri = uri, entity = body) ~> authorize
      sendTo(io).withResponsesReceivedBy(self)(rq)

      context become streaming
    case _ =>
  }

  def streaming: Receive = {
    case ChunkedResponseStart(_) =>
    case MessageChunk(entity, _) => addChunk(entity)
    case StopStreaming => io ! Http.Close; context become ready;
    case _ =>
  }

  override def deserializeTweet(t: String): Either[DeserializationException,Tweet] = {
    try {
      val json = JsonParser(t)
      Right(implicitly[RootJsonReader[Tweet]].read(json))
    }catch {
      case x: DeserializationException => Left(x)
    }}

  override def newTweet(t: Tweet): Unit = processor ! t
}