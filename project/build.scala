import sbt._
import Keys._

object JacksBuild extends Build {
  val buildSettings = Project.defaultSettings ++ Seq(
    name         := "jacks",
    version      := "2.0.2",
    organization := "com.lambdaworks",
    scalaVersion := "2.9.2",

    libraryDependencies <+= scalaVersion("org.scala-lang" % "scalap" % _),
    libraryDependencies ++= Seq(
      "com.fasterxml.jackson.core" % "jackson-databind" % "2.0.2",
      "org.scalatest" %% "scalatest" % "1.6.1" % "test",
      "org.scalacheck" %% "scalacheck" % "1.9" % "test",
      "junit" % "junit" % "4.10" % "test"
    ),

    scalacOptions ++= Seq("-unchecked", "-optimize"))

  val jacks = Project(id = "jacks", base = file("."), settings = buildSettings)
}
