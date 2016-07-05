import Dependencies._



lazy val tweets = (project in file("tweets")).
  settings(Commons.settings: _*).
  settings(
    libraryDependencies ++= tweetLibs
  )

lazy val root = (project in file(".")).
  settings(Commons.settings: _*).
  aggregate(tweets)