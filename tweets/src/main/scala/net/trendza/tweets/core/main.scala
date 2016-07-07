package net.trendza.tweets.core

/**
  * Created by mark on 05/07/16.
  */
import akka.actor.{Actor, ActorSystem, Props}

import scala.annotation.tailrec
import TweetStreamActor.{OpenStream, twitterUri}
import akka.actor.Actor.Receive
import net.trendza.tweets.domain.{Place, Tweet, User}
import java.io._

class PrintingActor extends Actor{
  val home = System.getProperty("user.home")
  val writer = new PrintWriter(new File(s"$home/projects/trendza/tweets.txt" ))
  override def receive: Receive = {
    case Tweet(id, User(uId,screen_name), text, _, _, created_at, _) => {writer.write(s"$screen_name tweeted $text"); writer.append("\n\r"); writer.flush(); }
  }
}


object Main extends App  {
  import Commands._

  val twitterUri = TweetStreamActor.twitterUri
  val system = ActorSystem()
  val logger = system.actorOf(Props(new PrintingActor()))
  val stream = system.actorOf(Props(new TweetStreamerActor(twitterUri, logger) with OAuthTwitterAuthorization))

  @tailrec
  private def commandLoop(): Unit = {
    scala.io.StdIn.readLine() match {
      case QuitCommand         => return
      case TrackCommand(query) => {
        println("gonna track " + query)
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

}