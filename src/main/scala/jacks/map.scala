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

class MapSerializer(t: JavaType, bp: BeanProperty) extends SerializerBase[Map[_, _]](t) {
  override def serialize(map: Map[_, _], g: JsonGenerator, p: SerializerProvider) {
    var kS, vS: JsonSerializer[AnyRef] = null
    var kC, vC: Class[_] = null

    g.writeStartObject()

    for ((k, v) <- map) {
      val kA = k.asInstanceOf[AnyRef]
      val vA = v.asInstanceOf[AnyRef]

      if (kA.getClass ne kC) {
        kC = kA.getClass
        kS = p.findKeySerializer(p.constructType(kC), bp)
      }
      kS.serialize(kA, g, p)

      if (vA ne null) {
        if (vA.getClass ne vC) {
          vC = vA.getClass
          vS = p.findValueSerializer(vC, bp)
        }
        vS.serialize(vA, g, p)
      } else {
        p.defaultSerializeNull(g)
      }
    }

    g.writeEndObject()
  }
}

abstract class GenMapDeserializer[K, V](kD: KeyDeserializer, vD: JsonDeserializer[_]) extends JsonDeserializer[Map[_, _]] {
  def newBuilder: Builder[(K, V), Map[K, V]]

  override def deserialize(p: JsonParser, ctx: DeserializationContext): Map[_, _] = {
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

class MapDeserializer[K, V](f: GenMapFactory[Map], kD: KeyDeserializer, vD: JsonDeserializer[_])
  extends GenMapDeserializer[K, V](kD, vD) {
    def newBuilder = f.newBuilder
}

class SortedMapDeserializer[K, V](f: SortedMapFactory[SortedMap], o: Ordering[K], kD: KeyDeserializer, vD: JsonDeserializer[_])
  extends GenMapDeserializer[K, V](kD, vD) {
    def newBuilder = f.newBuilder(o)
}
