/*
 * Copyright (c) 2015. La Marca Gaëtan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import play.PlayScala
import sbt._
import Keys._
import play.Play.autoImport._


object cuamBuild extends Build {

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

  val dbLibraries = Seq(
    "com.h2database" % "h2" % "1.4.187",
    "com.typesafe.slick" % "slick_2.11" % "2.1.0",
    "com.typesafe.play" %% "play-slick" % "0.8.1"
  )

  val securityLibraries = Seq(
    "org.mindrot" % "jbcrypt" % "0.3m"
  )


  lazy val root = Project("cuam", file(".")).enablePlugins(PlayScala) settings(
    name := """cuam""",
    version := "1.0-SNAPSHOT",
    scalaVersion := "2.11.6",
    resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
    libraryDependencies ++= basicLibraries ++ testLibraries ++ htmlLibraries ++ dbLibraries ++ securityLibraries
    )
}