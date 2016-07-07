package net.trendza.tweets.domain

import java.util.Date

/**
  * Created by mark on 05/07/16.
  */

case class User(id: String, screen_name: String)

case class Place(country: String, name: String, full_name: String) {
  override def toString = s"$name, $country"
}

case class Point(coordinates: Array[Double])
case class Tweet(id: Long, user: User, text: String, place: Option[Place], coordinates: Option[Point], created_at: Date, source: String)

