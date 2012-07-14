// Copyright (C) 2011 - Will Glozer.  All rights reserved.

package com.lambdaworks.jacks

import com.fasterxml.jackson.core._
import com.fasterxml.jackson.databind._
import com.fasterxml.jackson.databind.ser.std.StdSerializer

class OptionSerializer(t: JavaType) extends StdSerializer[Option[_]](t) {
  override def serialize(value: Option[_], g: JsonGenerator, p: SerializerProvider) {
    value match {
      case Some(v) =>
        val a = v.asInstanceOf[AnyRef]
        val s = p.findValueSerializer(a.getClass, null)
        s.serialize(a, g, p)
      case None =>
        p.defaultSerializeNull(g)
    }
  }

  override def isEmpty(v: Option[_]) = v.isEmpty
}

class OptionDeserializer(t: JavaType) extends JsonDeserializer[Option[_]] {
  override def deserialize(p: JsonParser, ctx: DeserializationContext): Option[_] = {
    val d = ctx.findContextualValueDeserializer(t, null)
    Some(d.deserialize(p, ctx))
  }

  override def getNullValue = None
}
