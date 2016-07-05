import sbt._
import Keys._

object Commons {
  val appVersion = "0.1"
  val scVersion = "2.11.8"

  val settings: Seq[Def.Setting[_]] = Seq(
    version := appVersion,
    scalaVersion := scVersion,
    scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")
  )
}