// Copyright (C) 2011 - Will Glozer.  All rights reserved.

package com.lambdaworks.jacks

import scala.collection._
import scala.collection.generic._
import scala.collection.mutable.Builder

import org.codehaus.jackson._
import org.codehaus.jackson.JsonToken._
import org.codehaus.jackson.map._
import org.codehaus.jackson.map.ser.std.SerializerBase
import org.codehaus.jackson.`type`.JavaType

class IterableSerializer(t: JavaType, bp: BeanProperty) extends SerializerBase[Iterable[_]](t) {
  override def serialize(iterable: Iterable[_], g: JsonGenerator, p: SerializerProvider) {
    var s: JsonSerializer[AnyRef] = null
    var c: Class[_] = null

    g.writeStartArray()

    for (v <- iterable) {
      val a = v.asInstanceOf[AnyRef]
      if (a ne null) {
        if (a.getClass ne c) {
          c = a.getClass
          s = p.findValueSerializer(c, bp)
        }
        s.serialize(a, g, p)
      } else {
        p.defaultSerializeNull(g)
      }
    }

    g.writeEndArray()
  }
}

abstract class IterableDeserializer[T, C](d: JsonDeserializer[_]) extends JsonDeserializer[C] {
  def newBuilder: Builder[T, C]

  override def deserialize(p: JsonParser, ctx: DeserializationContext): C = {
    val builder = newBuilder

    while (p.nextToken != END_ARRAY) {
      val value = p.getCurrentToken match {
        case VALUE_NULL => d.getNullValue
        case _          => d.deserialize(p, ctx)
      }
      builder += value.asInstanceOf[T]
    }

    builder.result
  }
}

class SeqDeserializer[T, C[T] <: GenTraversable[T]](c: GenericCompanion[C], d: JsonDeserializer[_])
  extends IterableDeserializer[T, C[_]](d) {
    def newBuilder = c.newBuilder[T]
}

class SortedSetDeserializer[T, C[T] <: SortedSet[T] with SortedSetLike[T, C[T]]]
  (c: SortedSetFactory[C], o: Ordering[T], d: JsonDeserializer[_]) extends IterableDeserializer[T, C[_]](d) {
    def newBuilder = c.newBuilder[T](o)
}

class BitSetDeserializer[C <: BitSet](c: BitSetFactory[BitSet], d: JsonDeserializer[_])
  extends IterableDeserializer[Int, BitSet](d) {
    def newBuilder = c.newBuilder
}

class OrderedDeserializer[T, C[T] <: Traversable[T] with GenericOrderedTraversableTemplate[T, C]]
  (c: OrderedTraversableFactory[C], o: Ordering[T], d: JsonDeserializer[_]) extends IterableDeserializer[T, C[_]](d) {
    def newBuilder = c.newBuilder[T](o)
}
