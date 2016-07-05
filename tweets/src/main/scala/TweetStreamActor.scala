/**
  * Created by trantem on 05/07/2016.
  */


import akka.actor._
import akka.io.IO
import spray.can.Http
import spray.http._

object TweetStreamActor {
  val twitterUri = Uri("https://stream.twitter.com/1.1/statuses/filter.json")
}

class TweetStreamerActor(uri: Uri, processor: ActorRef) extends Actor {
  val io = IO(Http)(context.system)

  def receive: Receive = {
    case query: String =>
      val body = HttpEntity(
        ContentType(MediaTypes.`application/x-www-form-urlencoded`),
        s"track=$query")
      val rq = HttpRequest(HttpMethods.POST, uri = uri, entity = body)
       sendTo(io).withResponsesReceivedBy(self)(rq)
    case MessageChunk(entity, _) =>
    case _ =>
  }
}