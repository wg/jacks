import sbt._
import Keys._

object JacksBuild extends Build {
  val buildSettings = Project.defaultSettings ++ Seq(
    name         := "jacks",
    version      := "2.0.5",
    organization := "com.lambdaworks",
    scalaVersion := "2.9.2",

    libraryDependencies <+= scalaVersion("org.scala-lang" % "scalap" % _),
    libraryDependencies ++= Seq(
      "com.fasterxml.jackson.core" % "jackson-databind" % "2.0.5",
      "org.scalatest" %% "scalatest" % "1.6.1" % "test",
      "org.scalacheck" %% "scalacheck" % "1.9" % "test"
    ),

    scalacOptions ++= Seq("-unchecked", "-optimize"),

    crossPaths              := false,
    publishArtifact in Test := false,
    publishMavenStyle       := true,
    publishTo <<= version {
      val nexus = "https://oss.sonatype.org/"
      _.trim.endsWith("SNAPSHOT") match {
        case false => Some("releases"  at nexus + "service/local/staging/deploy/maven2")
        case true  => Some("snapshots" at nexus + "content/repositories/snapshots")
      }
    },

    pomIncludeRepository := { _ => false },
    pomExtra             := (
      <url>http://github.com/wg/jacks</url>

      <licenses>
        <license>
          <name>Apache License, Version 2.0</name>
          <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
          <distribution>repo</distribution>
        </license>
      </licenses>

      <developers>
        <developer>
          <id>will</id>
          <name>Will Glozer</name>
        </developer>
      </developers>
    )
  )

  val jacks = Project(id = "jacks", base = file("."), settings = buildSettings)
}
