name := """play-react"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala, SbtWeb)

scalaVersion := "2.12.2"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.0" % Test
libraryDependencies += ws
libraryDependencies += "com.lihaoyi" %% "pprint" % "0.5.0"
libraryDependencies += "org.scala-debugger" %% "scala-debugger-api" % "1.1.0-M3"
libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.0"
libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.9.0"
libraryDependencies += "com.lihaoyi" % "ammonite-sshd" % "1.0.1" cross CrossVersion.full
libraryDependencies += "com.lihaoyi" % "ammonite" % "1.0.1" cross CrossVersion.full


// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.example.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.example.binders._"
