name := "zeroadv-collector"

version := "1.0"

scalaVersion := "2.10.3"

libraryDependencies ++= Seq(
  "org.zeromq" % "jeromq" % "0.3.2",
  "com.typesafe.akka" %% "akka-actor" % "2.2.3",
  "org.reactivemongo" %% "reactivemongo" % "0.10.0",
  "joda-time" % "joda-time" % "2.3",
  "org.joda" % "joda-convert" % "1.2",
  "com.typesafe" %% "scalalogging-slf4j" % "1.0.1",
  "ch.qos.logback" % "logback-classic" % "1.0.13",
  "com.softwaremill.macwire" %% "macros" % "0.5",
  "com.softwaremill.scalamacrodebug" %% "macros" % "0.3",
  "org.apache.commons" % "commons-math3" % "3.2",
  "org.scalatest" %% "scalatest" % "2.0" % "test",
  "org.mockito" % "mockito-all" % "1.9.5" % "test"
)

libraryDependencies <+= scalaVersion { "org.scala-lang" % "scala-swing" % _ }