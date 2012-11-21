// Copyright (C) 2011 - Will Glozer.  All rights reserved.

package com.lambdaworks.jacks

import scala.collection._
import scala.collection.generic._
import scala.collection.mutable.Builder

import com.fasterxml.jackson.core._
import com.fasterxml.jackson.core.JsonToken._
import com.fasterxml.jackson.databind._
import com.fasterxml.jackson.databind.ser.std.StdSerializer

class IterableSerializer(t: JavaType) extends StdSerializer[GenIterable[Any]](t) {
  override def serialize(iterable: GenIterable[Any], g: JsonGenerator, p: SerializerProvider) {
    var s: JsonSerializer[AnyRef] = null
    var c: Class[_] = null

    g.writeStartArray()

    for (v <- iterable) {
      val a = v.asInstanceOf[AnyRef]
      if (a ne null) {
        if (a.getClass ne c) {
          c = a.getClass
          s = p.findValueSerializer(c, null)
        }
        s.serialize(a, g, p)
      } else {
        p.defaultSerializeNull(g)
      }
    }

    g.writeEndArray()
  }

  override def isEmpty(v: GenIterable[Any]) = v.isEmpty
}

abstract class IterableDeserializer[T, C](t: JavaType) extends JsonDeserializer[C] {
  def newBuilder: Builder[T, C]

  override def deserialize(p: JsonParser, ctx: DeserializationContext): C = {
    val d = ctx.findContextualValueDeserializer(t, null)
    val builder = newBuilder

    if (!p.isExpectedStartArrayToken) throw ctx.mappingException(t.getRawClass)

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

class SeqDeserializer[T, C[T] <: GenTraversable[T]](c: GenericCompanion[C], t: JavaType)
  extends IterableDeserializer[T, C[_]](t) {
    def newBuilder = c.newBuilder[T]
}

class SortedSetDeserializer[T, C[T] <: SortedSet[T] with SortedSetLike[T, C[T]]]
  (c: SortedSetFactory[C], o: Ordering[T], t: JavaType) extends IterableDeserializer[T, C[_]](t) {
    def newBuilder = c.newBuilder[T](o)
}

class BitSetDeserializer[C <: BitSet](c: BitSetFactory[BitSet], t: JavaType)
  extends IterableDeserializer[Int, BitSet](t) {
    def newBuilder = c.newBuilder
}

class OrderedDeserializer[T, C[T] <: Traversable[T] with GenericOrderedTraversableTemplate[T, C]]
  (c: OrderedTraversableFactory[C], o: Ordering[T], t: JavaType) extends IterableDeserializer[T, C[_]](t) {
    def newBuilder = c.newBuilder[T](o)
}
