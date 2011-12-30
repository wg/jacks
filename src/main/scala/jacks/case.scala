// Copyright (C) 2011 - Will Glozer.  All rights reserved.

package com.lambdaworks.jacks

import org.codehaus.jackson._
import org.codehaus.jackson.JsonToken._
import org.codehaus.jackson.map._
import org.codehaus.jackson.map.ser.std.SerializerBase
import org.codehaus.jackson.`type`.JavaType

import java.lang.reflect.Constructor

class CaseClassSerializer(t: JavaType, accessors: Array[Accessor], bp: BeanProperty) extends SerializerBase[Product](t) {
  override def serialize(value: Product, g: JsonGenerator, p: SerializerProvider) {
    g.writeStartObject()

    for (i <- 0 until accessors.length) {
      val a = accessors(i)
      val v = value.productElement(i).asInstanceOf[AnyRef]
      val s = p.findValueSerializer(a.`type`, bp)

      g.writeFieldName(a.name)

      if (v != null) s.serialize(v, g, p) else p.defaultSerializeNull(g)
    }

    g.writeEndObject()
  }
}

class CaseClassDeserializer(c: Constructor[_], accessors: Array[Accessor], ds: Map[String, JsonDeserializer[_]]) extends JsonDeserializer[Any] {
  val fields = accessors.map(_.name -> None).toMap[String, Option[Object]]

  override def deserialize(p: JsonParser, ctx: DeserializationContext): Any = {
    var values = fields

    var t = p.getCurrentToken
    if (t == START_OBJECT) t = p.nextToken

    while (t == FIELD_NAME) {
      val name = p.getCurrentName
      val d    = ds.getOrElse(name, null)
      if (d ne null) {
        val value = p.nextToken match {
          case VALUE_NULL => d.getNullValue
          case _          => d.deserialize(p, ctx)
        }
        values = values.updated(name, Some(value.asInstanceOf[AnyRef]))
      } else {
        p.nextToken
        p.skipChildren
      }
      t = p.nextToken
    }

    val params = accessors.map { a =>
      values(a.name) match {
        case Some(v) => v
        case None    => default(a)
      }
    }

    c.newInstance(params: _*)
  }

  def default(a: Accessor) = a.default match {
    case Some(m) => m.invoke(null)
    case None    => null
  }
}
