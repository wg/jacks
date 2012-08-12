// Copyright (C) 2011 - Will Glozer.  All rights reserved.

package com.lambdaworks.jacks

import scala.collection._
import scala.collection.generic._
import scala.collection.mutable.Builder

import com.fasterxml.jackson.core._
import com.fasterxml.jackson.core.JsonToken._
import com.fasterxml.jackson.databind._
import com.fasterxml.jackson.databind.ser.std.StdSerializer

class MapSerializer(t: JavaType) extends StdSerializer[GenMap[Any, Any]](t) {
  override def serialize(map: GenMap[Any, Any], g: JsonGenerator, p: SerializerProvider) {
    var kS, vS: JsonSerializer[AnyRef] = null
    var kC, vC: Class[_] = null

    g.writeStartObject()

    for ((k, v) <- map) {
      val kA = k.asInstanceOf[AnyRef]
      val vA = v.asInstanceOf[AnyRef]

      if (kA.getClass ne kC) {
        kC = kA.getClass
        kS = p.findKeySerializer(p.constructType(kC), null)
      }
      kS.serialize(kA, g, p)

      if (vA ne null) {
        if (vA.getClass ne vC) {
          vC = vA.getClass
          vS = p.findValueSerializer(vC, null)
        }
        vS.serialize(vA, g, p)
      } else {
        p.defaultSerializeNull(g)
      }
    }

    g.writeEndObject()
  }

  override def isEmpty(v: GenMap[Any, Any]) = v.isEmpty
}

abstract class GenMapDeserializer[K, V](k: JavaType, v: JavaType) extends JsonDeserializer[GenMap[_, _]] {
  def newBuilder: Builder[(K, V), GenMap[K, V]]

  override def deserialize(p: JsonParser, ctx: DeserializationContext): GenMap[_, _] = {
    val kD = ctx.findKeyDeserializer(k, null)
    val vD = ctx.findContextualValueDeserializer(v, null)
    val builder = newBuilder

    while (p.nextToken == FIELD_NAME) {
      val name = p.getCurrentName

      val key = kD match {
        case kD:KeyDeserializer => kD.deserializeKey(name, ctx)
        case null               => name
      }

      val value = p.nextToken match {
        case VALUE_NULL => vD.getNullValue
        case _          => vD.deserialize(p, ctx)
      }

      builder += ((key.asInstanceOf[K], value.asInstanceOf[V]))
    }

    builder.result
  }
}

class MapDeserializer[K, V](f: GenMapFactory[GenMap], k: JavaType, v: JavaType)
  extends GenMapDeserializer[K, V](k, v) {
    def newBuilder = f.newBuilder
}

class SortedMapDeserializer[K, V](f: SortedMapFactory[SortedMap], o: Ordering[K], k: JavaType, v: JavaType)
  extends GenMapDeserializer[K, V](k, v) {
    def newBuilder = f.newBuilder(o)
}
