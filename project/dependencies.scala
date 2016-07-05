import sbt._
import Keys._

object Dependencies {
  val akkaV = "2.4.7"
  val sprayV = "1.3.3"

  val spraycan =      "io.spray"            %%    "spray-can"       % sprayV
  val sprayclient =   "io.spray"            %%    "spray-client"    % sprayV
  val sprayrouting =  "io.spray"            %%    "spray-routing"   % sprayV
  val sprayhttp =     "io.spray"            %%    "spray-http"      % sprayV
  val sprayhttpx =    "io.spray"            %%    "spray-httpx"     % sprayV
  val sprayutil =     "io.spray"            %%    "spray-util"      % sprayV
  val sprayjson =     "io.spray"            %%    "spray-json"      % "1.3.2"
  val spraytest =     "io.spray"            %%    "spray-testkit"   % sprayV  % "test"

  val akkacore =      "com.typesafe.akka"   %%    "akka-actor"      % akkaV
  val akkatest =      "com.typesafe.akka"   %%    "akka-testkit"    % akkaV   % "test"

  val sprayServerLibs = Seq(spraycan, sprayrouting)
  val sprayClientLibs = Seq(sprayclient, spraycan, sprayhttp, sprayhttpx, sprayutil, sprayjson)

  val tweetLibs = Seq(akkacore) ++ sprayClientLibs
}