package net.trendza.kafka

import akka.actor.{ActorSystem, Props}
import net.trendza.tweets.core.TweetStreamActor.{OpenStream, StopStreaming}
import net.trendza.tweets.core.{OAuthTwitterAuthorization, TweetStreamActor, TweetStreamerActor}

import scala.annotation.tailrec

/**
  * Created by mark on 10/07/16.
  */
object Main extends App  {
  import Commands._

  val twitterUri = TweetStreamActor.twitterUri
  val system = ActorSystem()
  val logger = system.actorOf(Props(new TweetProducerActor() with KafkaTweetSender))
  val stream = system.actorOf(Props(new TweetStreamerActor(twitterUri, logger) with OAuthTwitterAuthorization))

  @tailrec
  private def commandLoop(): Unit = {
    scala.io.StdIn.readLine() match {
      case QuitCommand         => return
      case CloseCommand       => stream ! StopStreaming
      case TrackCommand(query) => {
        stream ! OpenStream(query.split(","))
      }
      case _                   => println("WTF??!!")
    }

    commandLoop()
  }

  // start processing the commands
  commandLoop()

}

object Commands {

  val QuitCommand   = "quit"
  val TrackCommand = "track (.*)".r
  val CloseCommand   = "close"
}