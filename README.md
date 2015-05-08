#jacks - Jackson module for Scala

Jacks provides a Jackson Module for reading and writing common Scala data
types, including mutable & immutable collections, `Option`, `Tuple`, `Symbol`,
and case classes.

This version of jacks has been tested against Scala 2.11.0, 2.10.4 and
Jackson 2.3.3

Join the lambdaWorks-OSS Google Group to discuss this project:

http://groups.google.com/group/lambdaworks-oss

lambdaworks-oss@googlegroups.com

##Basic Usage

The JacksMapper companion object provides a preconfigured ObjectMapper
and a number of read/write methods:

```scala
JacksMapper.writeValueAsString(List(1, 2, 3)) 
// will return "[1,2,3]"
```
```scala
JacksMapper.readValue[List[Int]]("[1, 2, 3]")
// will return List(1, 2, 3)
```
The preconfigured `ObjectMapper` is available via `JacksMapper.mapper`
and a new `ScalaModule` may be added to any existing `ObjectMapper`.

##Erasure & Manifests

Reading parameterized types requires a Jackson JavaType which can be
derived from a Scala Manifest. The `readValue` methods of `JacksMapper` take
an implicit Manifest and look up the appropriate JavaType.

`JacksMapper.resolve(manifest)` will also return the correct JavaType
given a Manifest.

##Serialization Details

`Map`s are serialized as JSON objects, `Seq`s and `Set`s are serialized as
JSON arrays. `Tuple`s are also serialized as JSON arrays.

`None` is serialized as `null`, `Some(v)` is serialized as `v`.

`Symbol`s are serialized as strings.

Case classes are serialized as JSON objects.

##Collections

Jacks supports bidirectional mapping of nearly all Scala mutable and
immutable collections. Custom collections types are also supported as
long as they implement a supported interface and provide the
appropriate companion object.

##Case Classes

Jacks supports bidirectional mapping of case classes as JSON objects,
with a few restrictions: a `ScalaSig` is required, only fields declared
in the primary constructor are included, and inherited members are
ignored.

Jackson supports a rich set of annotations for modifying the default
(de)serialization of standard classes and their fields. Jacks will
not support all of these annotations for case classes, but does
support the following:

 - `@JsonProperty`
 - `@JsonIgnore`
 - `@JsonIgnoreProperties`
 - `@JsonInclude`
 - `@JsonCreator`

Jacks normally instantiates case classes via their primary constructor.
Alternatively a method of the companion object may be used instead by
annotating it with `@JsonCreator` and ensuring that each method parameter
is annotated with `@JsonProperty`:

```scala
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
```
##Maven Artifacts

Releases of jacks are available in the maven central repository:
```sbt
"com.lambdaworks" %% "jacks" % "2.3.3"`
```
```xml
<dependency>
  <groupId>com.lambdaworks</groupId>
  <artifactId>jacks_2.11</artifactId>
  <version>2.3.3</version>
</dependency>
```