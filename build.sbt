name := "test-web-service"

version := "0.1"

scalaVersion := "2.11.9"

libraryDependencies ++= Seq(
  //akka-http
  "com.typesafe.akka" %% "akka-http"   % "10.1.1",
  "com.typesafe.akka" %% "akka-stream" % "2.5.11",

  //akka-xml-support
  "com.typesafe.akka" %% "akka-http-xml" % "10.1.1",

  //akka-http-testkit
  "com.typesafe.akka" %% "akka-http-testkit" % "10.1.1",

  //guice
  "com.google.inject" % "guice" % "4.1.0",
  "com.google.inject.extensions" % "guice-throwingproviders" % "4.1.0",

  //csv-parser/writer
  "com.github.tototoshi" %% "scala-csv" % "1.3.5",

  //scalactic
  "org.scalactic" %% "scalactic" % "3.0.5",

  //scala test
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",

  //pure config
  "com.github.pureconfig" %% "pureconfig" % "0.9.1"
)