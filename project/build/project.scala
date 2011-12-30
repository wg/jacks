import sbt._

class project(info: ProjectInfo) extends DefaultProject(info) {
  val codehaus = "jackson.codehaus.org" at "http://repository.codehaus.org"

  override def libraryDependencies = Set(
    "org.codehaus.jackson" % "jackson-mapper-asl" % "1.9.3",
    "org.scala-lang" % "scalap" % crossScalaVersionString,

    "org.scalatest" % ("scalatest_" + crossScalaVersionString) % "1.6.1" % "test",
    "org.scala-tools.testing" % ("scalacheck_" + crossScalaVersionString) % "1.9" % "test",

    "junit" % "junit" % "4.10" % "test"
  ) ++ super.libraryDependencies

  override def compileOptions = super.compileOptions ++ Seq(Unchecked, Optimize)
}
