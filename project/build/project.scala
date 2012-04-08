import sbt._

class project(info: ProjectInfo) extends DefaultProject(info) {
  override def libraryDependencies = Set(
    "com.fasterxml.jackson.core" % "jackson-databind" % "2.0.0",
    "org.scala-lang" % "scalap" % crossScalaVersionString,

    "org.scalatest" % ("scalatest_" + crossScalaVersionString) % "1.6.1" % "test",
    "org.scala-tools.testing" % ("scalacheck_" + crossScalaVersionString) % "1.9" % "test",

    "junit" % "junit" % "4.10" % "test"
  ) ++ super.libraryDependencies

  override def compileOptions = super.compileOptions ++ Seq(Unchecked, Optimize)
}
