import sbt._
import Keys._

object JacksBuild extends Build {
  val buildSettings = Project.defaultSettings ++ Seq(
    name         := "jacks",
    version      := "2.2.0",
    organization := "com.lambdaworks",
    scalaVersion := "2.10.1",

    crossScalaVersions := Seq("2.10.1", "2.9.3"),

    libraryDependencies <+= scalaVersion("org.scala-lang" % "scalap" % _),
    libraryDependencies ++= Seq(
      "com.fasterxml.jackson.core" % "jackson-databind" % "2.2.0",
      "org.scalatest" %% "scalatest" % "1.9.1" % "test",
      "org.scalacheck" %% "scalacheck" % "1.10.1" % "test"
    ),

    scalacOptions ++= Seq("-unchecked", "-optimize"),
    scalacOptions <++= scalaVersion map {
      CrossVersion.partialVersion(_) match {
        case Some((m, n)) if m > 2 || n >= 10 => Seq("-language:_")
        case _                                => Nil
      }
    },

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

      <scm>
        <connection>scm:git:git://github.com/wg/jacks.git</connection>
        <developerConnection>scm:git:git://github.com/wg/jacks.git</developerConnection>
        <url>http://github.com/wg/jacks</url>
      </scm>

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
