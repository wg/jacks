name := "jacks"

version := "2.1.2"

organization := "com.lambdaworks"

scalaVersion := "2.10.0"

resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.3.0-SNAPSHOT")

libraryDependencies ++= Seq(
      "com.fasterxml.jackson.core"  %   "jackson-databind"  % "2.1.2",
      "org.scalacheck"              %%  "scalacheck"        % "1.10.0"  % "test",
      "org.scalatest"               %   "scalatest_2.10.0"  % "1.8"     % "test"
)

scalacOptions ++= Seq("-unchecked", "-optimize")

publishArtifact in Test := false

publishMavenStyle       := true

publishTo <<= version {
      val nexus = "https://oss.sonatype.org/"
      _.trim.endsWith("SNAPSHOT") match {
        case false => Some("releases"  at nexus + "service/local/staging/deploy/maven2")
        case true  => Some("snapshots" at nexus + "content/repositories/snapshots")
      }
    }

pomIncludeRepository := { _ => false }

pomExtra := (
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
      </developers>)




