import play.PlayScala

name := """cuam"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

val basicLibraries = Seq(
  jdbc,
  anorm,
  cache,
  ws
)

val testLibraries = Seq(
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "org.scalacheck" %% "scalacheck" % "1.12.2" % "test"
)

val htmlLibraries = Seq(
  "org.webjars" %% "webjars-play" % "2.3.0-2",
  "org.webjars.bower" % "bootstrap" % "3.3.4",
  "org.webjars.bower" % "jquery" % "2.1.3",
  "org.webjars.bower" % "bootswatch" % "3.3.4"
)

val dbLibraries = Seq (
  "com.typesafe.slick" % "slick_2.11" % "2.1.0",
  "com.typesafe.play" %% "play-slick" % "0.8.1"
)

libraryDependencies ++= basicLibraries ++ testLibraries ++ htmlLibraries ++ dbLibraries
