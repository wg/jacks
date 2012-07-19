// Copyright (C) 2011 - Will Glozer.  All rights reserved.

package com.lambdaworks.jacks

import com.fasterxml.jackson.annotation.JsonInclude.Include._
import com.fasterxml.jackson.core._
import com.fasterxml.jackson.core.JsonToken._
import com.fasterxml.jackson.databind._
import com.fasterxml.jackson.databind.ser.std.StdSerializer

import java.lang.reflect.Constructor

class CaseClassSerializer(t: JavaType, accessors: Array[Accessor]) extends StdSerializer[Product](t) {
  override def serialize(value: Product, g: JsonGenerator, p: SerializerProvider) {
    g.writeStartObject()

    for (i <- 0 until accessors.length) {
      val a = accessors(i)
      val v = value.productElement(i).asInstanceOf[AnyRef]
      val s = p.findValueSerializer(a.`type`, null)
      if (!a.ignored && include(a, s, v)) {
        g.writeObjectField(a.name, v)
      }
    }

    g.writeEndObject()
  }

  @inline final def include(a: Accessor, s: JsonSerializer[AnyRef], v: AnyRef): Boolean = a.include match {
    case ALWAYS      => true
    case NON_DEFAULT => default(a) != v
    case NON_EMPTY   => !s.isEmpty(v)
    case NON_NULL    => v != null
  }

  @inline final def default(a: Accessor) = a.default match {
    case Some(m) => m.invoke(null)
    case None    => null
  }
}

class CaseClassDeserializer(c: Constructor[_], accessors: Array[Accessor]) extends JsonDeserializer[Any] {
  val fields = accessors.map(a => a.name -> None).toMap[String, Option[Object]]
  val types  = accessors.map(a => a.name -> a.`type`).toMap

  override def deserialize(p: JsonParser, ctx: DeserializationContext): Any = {
    var values = fields

    var token = p.getCurrentToken
    if (token == START_OBJECT) token = p.nextToken

    while (token == FIELD_NAME) {
      val name = p.getCurrentName
      val t    = types.getOrElse(name, null)
      if (t ne null) {
        val d = ctx.findContextualValueDeserializer(t, null)
        val value = p.nextToken match {
          case VALUE_NULL => d.getNullValue
          case _          => d.deserialize(p, ctx)
        }
        values = values.updated(name, Some(value.asInstanceOf[AnyRef]))
      } else {
        p.nextToken
        p.skipChildren
      }
      token = p.nextToken
    }

    val params = accessors.map { a =>
      values(a.name) match {
        case Some(v) => v
        case None    => default(a)
      }
    }

    c.newInstance(params: _*)
  }

  @inline final def default(a: Accessor) = a.default match {
    case Some(m) => m.invoke(null)
    case None    => null
  }
}
