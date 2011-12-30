// Copyright (C) 2011 - Will Glozer.  All rights reserved.

package com.lambdaworks.jacks

import org.codehaus.jackson._
import org.codehaus.jackson.map._
import org.codehaus.jackson.map.ser.std.SerializerBase
import org.codehaus.jackson.`type`.JavaType

class OptionSerializer(t: JavaType, bp: BeanProperty) extends SerializerBase[Option[_]](t) {
  override def serialize(value: Option[_], g: JsonGenerator, p: SerializerProvider) {
    value match {
      case Some(v) =>
        val a = v.asInstanceOf[AnyRef]
        val s = p.findValueSerializer(a.getClass, bp)
        s.serialize(a, g, p)
      case None =>
        p.defaultSerializeNull(g)
    }
  }
}

class OptionDeserializer(d: JsonDeserializer[_]) extends JsonDeserializer[Option[_]] {
  override def deserialize(p: JsonParser, ctx: DeserializationContext): Option[_] = {
    Some(d.deserialize(p, ctx))
  }

  override def getNullValue = None
}
