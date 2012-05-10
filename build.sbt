name := "jacks"

version := "2.0.1"

organization := "lambdaworks"

crossScalaVersions := Seq("2.9.1", "2.9.2")

libraryDependencies ++= Seq(
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.0.1",
  "org.scalatest" %% "scalatest" % "1.6.1" % "test",
  "org.scalacheck" %% "scalacheck" % "1.9" % "test",
  "junit" % "junit" % "4.10" % "test"
)

libraryDependencies <<= (scalaVersion, libraryDependencies) { (sv, deps) =>
  deps :+ ("org.scala-lang" % "scalap" % sv)
}
