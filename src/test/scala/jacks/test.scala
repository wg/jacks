// Copyright (C) 2011 - Will Glozer.  All rights reserved.

package com.lambdaworks.jacks

import com.fasterxml.jackson.annotation._
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectReader

import org.scalatest.FunSuite
import org.scalatest.Matchers

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
   seq:     Seq[Int]         = Seq(1, 3),
   vector:  Vector[Int]      = Vector(1, 3),
   string:  String           = "foo"
)

case class Parameterized[T](value: T)
case class NoDefault(int: Int, string: String)
case class Constructors(var value: String) {
  private def this() { this(null) }
  private def this(int: Int) { this(int.toString) }
}

case class Empty(
  @JsonInclude(Include.NON_EMPTY) list:   List[Int]        = Nil,
  @JsonInclude(Include.NON_EMPTY) map:    Map[String, Int] = Map.empty,
  @JsonInclude(Include.NON_EMPTY) option: Option[Int]      = None
)

case class PropertyAnnotation(@JsonProperty("FOO") foo: String)
case class PropertyRequired(@JsonProperty(required = true) foo: String)
case class IgnoreAnnotation(@JsonIgnore foo: String = null, bar: String)
case class IncludeAnnotation(
  @JsonInclude(Include.ALWAYS)      always:     Int         = 0,
  @JsonInclude(Include.NON_DEFAULT) nonDefault: Int         = 1,
  @JsonInclude(Include.NON_EMPTY)   nonEmpty:   Option[Int] = None,
  @JsonInclude(Include.NON_NULL)    nonNull:    String      = null
)

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(Array("ignored"))
case class ClassAnnotations(foo: String = null, ignored: String = "bar")

sealed trait AB { @JsonValue def jsonValue: String }
case object A extends AB { def jsonValue = "A" }
case object B extends AB { def jsonValue = "B" }

object Editor extends Enumeration {
  val vi, emacs = Value
}

case class KitchenSink[T](foo: String, bar: T, v: AB, ed: Editor.Value)

object KitchenSink {
  @JsonCreator
  def create[T](
    @JsonProperty("foo") foo: String,
    @JsonProperty("bar") bar: T,
    @JsonProperty("v")   v:   String = "A",
    @JsonProperty("ed")  ed:  String = "emacs"
  ): KitchenSink[T] = KitchenSink[T](foo, bar, if (v == "A") A else B, Editor.withName(ed))
}

object Outer {
  case class Nested(s: String)
}

case class NamingStrategy(camelCase: String, PascalCase: Int)

case class WithAny(map: Map[String, Any])

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
    rw(Aliased(list   = List('B')))     should equal (Aliased(list   = List('B')))
    rw(Aliased(map    = Map("A" -> 1))) should equal (Aliased(map    = Map("A" -> 1)))
    rw(Aliased(set    = Set(2, 4)))     should equal (Aliased(set    = Set(2, 4)))
    rw(Aliased(seq    = Seq(1, 2)))     should equal (Aliased(seq    = Seq(1, 2)))
    rw(Aliased(vector = Vector(1, 2)))  should equal (Aliased(vector = Vector(1, 2)))
    rw(Aliased(string = "test"))        should equal (Aliased(string = "test"))
  }

  test("parameterized types correct") {
    rw(Parameterized[String]("foo")) should equal (Parameterized[String]("foo"))
    rw(Parameterized[Int](1)) should equal (Parameterized[Int](1))
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

  test("JsonGenerator.isEmpty correct") {
    write(Empty())                      should equal ("""{}""")
    write(Empty(list = List(1)))        should equal ("""{"list":[1]}""")
    write(Empty(map = Map("one" -> 1))) should equal ("""{"map":{"one":1}}""")
    write(Empty(option = Some(1)))      should equal ("""{"option":1}""")
  }

  test("@JsonProperty handled correctly") {
    val json = """{"FOO":"a"}"""
    write(PropertyAnnotation("a")) should equal (json)
    read[PropertyAnnotation](json) should equal (PropertyAnnotation("a"))
  }

  test("@JsonProperty(required = true) handled correctly") {
    intercept[JsonMappingException] { read[PropertyRequired]("""{}""") }
    rw(PropertyRequired(null)) should equal (PropertyRequired(null))
  }

  test("@JsonIgnore handled correctly") {
    val json = """{"bar":"b"}"""
    write(IgnoreAnnotation("a", "b")) should equal (json)
    read[IgnoreAnnotation](json) should equal (IgnoreAnnotation(null, "b"))
  }

  test("@JsonIgnoreProperties handled correctly") {
    write(ClassAnnotations("a")) should equal ("""{"foo":"a"}""")
  }

  test("@JsonInclude handled correctly") {
    write(IncludeAnnotation())                   should equal ("""{"always":0}""")
    write(IncludeAnnotation(nonDefault = 3))     should equal ("""{"always":0,"nonDefault":3}""")
    write(IncludeAnnotation(nonEmpty = Some(2))) should equal ("""{"always":0,"nonEmpty":2}""")
    write(IncludeAnnotation(nonNull = "a"))      should equal ("""{"always":0,"nonNull":"a"}""")

    write(ClassAnnotations())    should equal ("""{}""")
    write(ClassAnnotations("a")) should equal ("""{"foo":"a"}""")
  }

  test("@JsonCreator handled correctly") {
    val kss = KitchenSink[String]("f", "s", B, Editor.vi)
    val ksi = KitchenSink[Int]("g", 1, A, Editor.emacs)
    rw(kss) should equal (kss)
    read[KitchenSink[Int]]("""{"foo":"g","bar":1}""") should equal (ksi)
  }

  test("nested case classes handled correctly") {
    rw(Outer.Nested("foo")) should equal (Outer.Nested("foo"))
  }

  test("PropertyNamingStrategy is used correctly") {
    import com.fasterxml.jackson.databind.PropertyNamingStrategy._

    val m = new Object with JacksMapper
    m.mapper.setPropertyNamingStrategy(CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES)

    val obj  = NamingStrategy("foo", 1)
    val json = """{"camel_case":"foo","pascal_case":1}"""

    m.writeValueAsString(obj)         should equal (json)
    m.readValue[NamingStrategy](json) should equal (obj)
  }

  test("Map[String, Any] handled correctly") {
    val map = Map[String, Any]("foo" -> 1, "bar" -> "2")
    rw(WithAny(map)) should equal (WithAny(map))
  }
}

class InvalidJsonSuite extends JacksTestSuite {
  test("deserializing case class from non-object throws JsonMappingException") {
    intercept[JsonMappingException] { read[Primitives]("123") }
  }

  test("deserializing Iterable from non-array throws JsonMappingException") {
    intercept[JsonMappingException] { read[List[Int]]("123") }
  }

  test("deserializing Map from non-object throws JsonMappingException") {
    intercept[JsonMappingException] { read[Map[String, Any]]("123") }
  }

  test("deserializing Tuple from non-array throws JsonMappingException") {
    intercept[JsonMappingException] { read[Tuple2[Any, Any]]("123") }
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

class UntypedObjectDeserializerSuite extends JacksTestSuite {
  import com.fasterxml.jackson.databind.DeserializationFeature._
  import JacksMapper._

  test("reading JSON object returns Scala Map") {
    read[AnyRef]("""{"one":1,"two":[2]}""") should equal (Map("one" -> 1, "two" -> List(2)))
  }

  test("reading JSON array returns Scala List") {
    read[AnyRef]("[1, 2, 3]") should equal (List(1, 2, 3))
  }

  test("reading JSON array returns Java Array") {
    val r = mapper.reader[ObjectReader](USE_JAVA_ARRAY_FOR_JSON_ARRAY).forType(classOf[AnyRef])
    r.readValue[AnyRef]("[1, 2, 3]") should equal (Array(1, 2, 3))
  }
}

trait JacksTestSuite extends FunSuite with Matchers {
  import JacksMapper._

  def rw[T: Manifest](v: T)        = read(write(v))
  def write[T: Manifest](v: T)     = writeValueAsString(v)
  def read[T: Manifest](s: String) = readValue(s)
}
