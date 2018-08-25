name := """simple-bug-tracker"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.6"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test

libraryDependencies += "com.typesafe.play" %% "play-slick" % "3.0.3"
libraryDependencies += "com.typesafe.play" %% "play-slick-evolutions" % "3.0.3"

libraryDependencies += "org.postgresql" % "postgresql" % "42.2.4"

libraryDependencies += "org.apache.lucene" % "lucene-core" % "7.4.0"
libraryDependencies += "org.apache.lucene" % "lucene-analyzers-common" % "7.4.0"
libraryDependencies += "org.apache.lucene" % "lucene-queryparser" % "7.4.0"