package net.trendza.tweets.core

import java.util.Date

import net.trendza.tweets.domain.{Place, Point, Tweet, User}
import spray.json.{DefaultJsonProtocol, JsString, JsValue, JsonFormat}

/**
  * Created by mark on 10/07/16.
  */
object TweetJsonProtocol extends DefaultJsonProtocol {
  implicit object DateFormater extends JsonFormat[Date]{

    val format = new java.text.SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy")
    override def write(obj: Date): JsValue = {
      JsString(format.format(obj))
    }

    override def read(json: JsValue): Date = json match {
      case JsString(sval) => {
        format.parse(sval)
      }
    }
  }

  implicit val UserFormat = jsonFormat2(User)
  implicit val PlaceFormat = jsonFormat3(Place)
  implicit val PointFormat = jsonFormat1(Point)
  implicit val TweetFormat = jsonFormat7(Tweet)
}
