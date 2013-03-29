// Copyright (C) 2011 - Will Glozer.  All rights reserved.

package com.lambdaworks.jacks

import com.fasterxml.jackson.annotation.JsonInclude.Include._
import com.fasterxml.jackson.core._
import com.fasterxml.jackson.core.JsonToken._
import com.fasterxml.jackson.databind._
import com.fasterxml.jackson.databind.ser.std.StdSerializer

import java.lang.reflect.{Constructor, Method}

class CaseClassSerializer(t: JavaType, accessors: Array[Accessor], skipNulls: Boolean) extends StdSerializer[Product](t) {
  override def serialize(value: Product, g: JsonGenerator, p: SerializerProvider) {
    g.writeStartObject()

    for (i <- 0 until accessors.length) {
      val a = accessors(i)
      val v = value.productElement(i).asInstanceOf[AnyRef]
      val s = p.findValueSerializer(a.`type`, null)
      if (!a.ignored && include(a, s, v)) {
        g.writeFieldName(a.name)
        if (v != null) s.serialize(v, g, p) else p.defaultSerializeNull(g)
      }
    }

    g.writeEndObject()
  }

  @inline final def include(a: Accessor, s: JsonSerializer[AnyRef], v: AnyRef): Boolean = a.include match {
    case Some(ALWAYS)      => true
    case Some(NON_DEFAULT) => default(a) != v
    case Some(NON_EMPTY)   => !s.isEmpty(v)
    case Some(NON_NULL)    => v != null
    case None              => {
      // unfortunately, while Jackson serializers have an isEmpty() to match the getEmptyValue() of deserializers,
      // they lack an isNull() to match the getNullValue() of deserializers.
      // We cannot use isEmpty, as otherwise empty iterables would also match. Therefore, the only
      // viable solution here is to match Option's None explicitly, unfortunately.
      //
      // null or None should /not/ be skipped it there is an explicit default, unless the default
      // happens to be null or None. But do not skip other values just because they match the default.
      !skipNulls || (v != null && v != None) || (!hasNoDefault(a) && {
        val df=default(a)
        (df != null && df != None)
      })
    }
  }

  @inline final def default(a: Accessor) = a.default match {
    case Some(m) => m.invoke(null)
    case None    => null
  }

  @inline final def hasNoDefault(a: Accessor) = a.default == None
}

class CaseClassDeserializer(t: JavaType, c: Creator, checkNulls: Boolean) extends JsonDeserializer[Any] {
  val fields = c.accessors.map(a => a.name -> None).toMap[String, Option[Object]]
  val types  = c.accessors.map(a => a.name -> a.`type`).toMap

  override def deserialize(p: JsonParser, ctx: DeserializationContext): Any = {
    var values = fields

    if (p.getCurrentToken != START_OBJECT) throw ctx.mappingException(t.getRawClass)
    var token = p.nextToken

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

    val params = c.accessors.map { a =>
      values(a.name) match {
        case Some(v) => v
        case None    => {
          if (checkNulls) {
            // refuse to store nulls into case class fields, unless
            // explicitly requested with a default value
            // In case of Option, a missing property becomes None

            if (c.hasNoDefault(a)) {
              val d = ctx.findContextualValueDeserializer(a.`type`, null)
              val e = d.getNullValue
              if (e != null) e else
                throw ctx.mappingException("Required property '"+a.name+"' is missing.")
            } else {
              // c hasDefault(a), hence return the default
              c.default(a)
            }
          } else {
            // Jacks 2.1.4 behavior will use c.default().
            // default() should use d.getNullValue, but instead
            // always returns null (even for Option)
            c.default(a)
          }
        }
      }
    }

    c(params)
  }
}

trait Creator {
  val accessors: Array[Accessor]
  def apply(args: Seq[AnyRef]): Any
  def default(a: Accessor): AnyRef
  def hasNoDefault(a: Accessor) = a.default == None
}

class ConstructorCreator(c: Constructor[_], val accessors: Array[Accessor]) extends Creator {
  def apply(args: Seq[AnyRef]) = c.newInstance(args: _*)

  def default(a: Accessor) = a.default match {
    case Some(m) => m.invoke(null)
    case None    => null
  }
}

class CompanionCreator(m: Method, c: Object, val accessors: Array[Accessor]) extends Creator {
  def apply(args: Seq[AnyRef]) = m.invoke(c, args: _*)

  def default(a: Accessor) = a.default match {
    case Some(m) => m.invoke(c)
    case None    => null
  }
}
