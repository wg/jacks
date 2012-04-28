// Copyright (C) 2011 - Will Glozer.  All rights reserved.

package com.lambdaworks.jacks

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

case class Primitives(
  boolean: Boolean = true,
  byte:    Byte    = 0,
  char:    Char    = 'A',
  double:  Double  = 3.14,
  float:   Float   = 1.23f,
  int:     Int     = 42,
  long:    Long    = 2L,
  short:   Short   = 0
)

case class Arrays(
  bytes:   Array[Byte]   = Array(1, 2, 3),
  strings: Array[String] = Array("a", "b")
)

case class Aliased(
   list:    List[Char]       = List('A'),
   map:     Map[String, Int] = Map("one" -> 1),
   set:     Set[Long]        = Set(1, 2, 3),
   string:  String           = "foo"
)

case class Parameterized[T](value: T)
case class NoDefault(int: Int, string: String)
case class Constructors(var value: String) {
  private def this() { this(null) }
  private def this(int: Int) { this(int.toString) }
}

class CaseClassSuite extends JacksTestSuite {
  test("primitive types correct") {
    rw(Primitives(boolean = false)) should equal (Primitives(boolean = false))
    rw(Primitives(byte    = 1))     should equal (Primitives(byte    = 1))
    rw(Primitives(char    = 'B'))   should equal (Primitives(char    = 'B'))
    rw(Primitives(double  = 1.23))  should equal (Primitives(double  = 1.23))
    rw(Primitives(float   = 3.14f)) should equal (Primitives(float   = 3.14f))
    rw(Primitives(int     = 0))     should equal (Primitives(int     = 0))
    rw(Primitives(long    = 1L))    should equal (Primitives(long    = 1L))
    rw(Primitives(short   = 4))     should equal (Primitives(short   = 4))
  }

  test("default used correctly") {
    rw(Primitives()).boolean should equal (true)
    rw(Primitives()).byte    should equal (0)
    rw(Primitives()).char    should equal ('A')
    rw(Primitives()).double  should equal (3.14)
    rw(Primitives()).float   should equal (1.23f)
    rw(Primitives()).int     should equal (42)
    rw(Primitives()).long    should equal (2L)
    rw(Primitives()).short   should equal (0)

    rw(Arrays()).bytes should equal (Array[Byte](1, 2, 3))
    rw(Aliased()).list should equal (List[Char]('A'))
  }

  test("array types correct") {
    rw(Arrays(bytes   = Array(1, 2))).bytes       should equal (Array[Byte](1, 2))
    rw(Arrays(strings = Array("1", "2"))).strings should equal (Array[String]("1", "2"))
  }

  test("aliased types correct") {
    rw(Aliased(list = List('B')))     should equal (Aliased(list = List('B')))
    rw(Aliased(map  = Map("A" -> 1))) should equal (Aliased(map  = Map("A" -> 1)))
    rw(Aliased(set  = Set(2, 4)))     should equal (Aliased(set  = Set(2, 4)))
    rw(Aliased(string = "test"))      should equal (Aliased(string = "test"))
  }

  test("parameterized types correct") {
    rw(Parameterized[String]("foo")) should equal (Parameterized[String]("foo"))
    rw(Parameterized[List[Int]](List(1))) should equal (Parameterized[List[Int]](List(1)))
  }

  test("unknown properties skipped") {
    val json = """{"skip": [1], "set": [17, 23]}"""
    read[Aliased](json) should equal (Aliased(set = Set(17, 23)))
  }

  test("property without default is null") {
    read[NoDefault]("""{"int": 1}""").string should equal (null)
    rw(NoDefault(1, null)) should equal (NoDefault(1, null))
  }

  test("correct constructor found") {
    rw(Constructors("value")) should equal (Constructors("value"))
  }
}

class JacksMapperSuite extends JacksTestSuite {
  import java.io._
  import java.nio.charset.Charset
  import JacksMapper._

  val ASCII = Charset.forName("US-ASCII")

  test("readValue overloads correct") {
    val json   = "[1, 2, 3]"

    val bytes  = json.getBytes(ASCII)
    val stream = new ByteArrayInputStream(bytes)
    val reader = new InputStreamReader(new ByteArrayInputStream(bytes), ASCII)

    readValue[List[Int]](bytes)  should equal (List(1, 2, 3))
    readValue[List[Int]](stream) should equal (List(1, 2, 3))
    readValue[List[Int]](reader) should equal (List(1, 2, 3))
  }

  test("writeValue overloads correct") {
    val list  = List(1, 2, 3)
    val bytes = writeValueAsString(list).getBytes(ASCII)

    var stream = new ByteArrayOutputStream
    writeValue(stream, list)
    stream.toByteArray should equal (bytes)

    stream = new ByteArrayOutputStream
    writeValue(new OutputStreamWriter(stream, ASCII), list)
    stream.toByteArray should equal (bytes)
  }

  test("resolve caches JavaType") {
    val m = manifest[String]
    resolve(m) should be theSameInstanceAs resolve(m)
  }
}

trait JacksTestSuite extends FunSuite with ShouldMatchers {
  import JacksMapper._

  def rw[T: Manifest](v: T)        = read(write(v))
  def write[T: Manifest](v: T)     = writeValueAsString(v)
  def read[T: Manifest](s: String) = readValue(s)
}
