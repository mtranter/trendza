import Dependencies._



lazy val tweets = (project in file("tweets")).
  settings(Commons.settings: _*).
  settings(
    libraryDependencies ++= tweetLibs
  )


lazy val kafkatweetproducer = (project in file("kafkatweetproducer")).
  settings(Commons.settings: _*).
  settings(
    libraryDependencies ++= kafkaLibs
  ).
  dependsOn(tweets)

lazy val crunching = (project in file("crunching")).
  settings(Commons.settings: _*).
  settings(
    libraryDependencies ++= crunchingLibs
  ).
  dependsOn(tweets)

lazy val root = (project in file(".")).
  settings(Commons.settings: _*).
  aggregate(tweets, kafkatweetproducer).
  dependsOn(tweets, kafkatweetproducer)