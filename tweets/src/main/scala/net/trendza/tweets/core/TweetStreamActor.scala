package net.trendza.tweets.core

/**
  * Created by trantem on 05/07/2016.
  */


import java.util.Date

import akka.actor.{Actor, ActorRef}
import akka.io.IO
import net.trendza.tweets.core.OAuth._
import net.trendza.tweets.core.TweetStreamActor.OpenStream
import net.trendza.tweets.domain._
import spray.can.Http
import spray.client.pipelining._
import spray.http.{HttpRequest, MessageChunk, _}
import spray.json._
import spray.httpx.unmarshalling._

import scala.io.Source
import scala.util.Try

object MyJsonProtocol extends DefaultJsonProtocol {
  implicit object DateFormater extends JsonFormat[Date]{
    override def write(obj: Date): JsValue = ???

    override def read(json: JsValue): Date = {
      val format = new java.text.SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy")
      format.parse(json.toString())
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
import MyJsonProtocol._
trait TweetMarshaller {

   object TweetUnmarshaller {

     implicit def sprayJsonUnmarshaller[T: RootJsonReader]: Unmarshaller[T] =
       new Unmarshaller[T] {
         override def apply(v1: HttpEntity): Deserialized[T] = v1 match {
           case x: HttpEntity.NonEmpty ⇒
             val json = JsonParser(x.asString(defaultCharset = HttpCharsets.`UTF-8`))
             println(json)
             try {
               Right(implicitly[RootJsonReader[T]].read(json))
             }catch {
               case x: DeserializationError => Left(x)
             }
         }
       }


    def apply(entity: HttpEntity): Deserialized[Tweet] = {
      Try {
        entity.as[Tweet]
      }
    }.getOrElse(Left(MalformedContent("bad json: " + entity.asString)))
  }
}

object TweetStreamActor {

  val twitterUri = Uri("https://stream.twitter.com/1.1/statuses/filter.json")

  case class OpenStream(follow: Array[String])
}

class TweetStreamerActor(uri: Uri, processor: ActorRef) extends Actor with TweetMarshaller {
  this: TwitterAuthorization =>
  val io = IO(Http)(context.system)

  def receive: Receive = ready

  def ready: Receive = {
    case OpenStream(follow) =>
      val body = HttpEntity(
        ContentType(MediaTypes.`application/x-www-form-urlencoded`),
        s"delimited=length&track=${follow mkString ","}")
      val rq =  HttpRequest(HttpMethods.POST, uri = uri, entity = body) ~> authorize
      sendTo(io).withResponsesReceivedBy(self)(rq)
      context become connected
  }

  def connected: Receive = {
    case ChunkedResponseStart(_) =>
    case MessageChunk(entity, _) => TweetUnmarshaller(entity).fold(x =>  throw new Error(x.toString), processor !)
    case r@HttpResponse(status,entity, _,_) => {
      if(status.isSuccess){
        TweetUnmarshaller(entity).fold(_ => (), processor !)
      }else {
        throw new Error("Connection failed: " + status)
      }
    }
   }
}