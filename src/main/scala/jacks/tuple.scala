// Copyright (C) 2011 - Will Glozer.  All rights reserved.

package com.lambdaworks.jacks

import org.codehaus.jackson._
import org.codehaus.jackson.JsonToken._
import org.codehaus.jackson.map._
import org.codehaus.jackson.map.ser.std.SerializerBase
import org.codehaus.jackson.`type`.JavaType

import java.lang.reflect.Constructor

class TupleSerializer(t: JavaType, bp: BeanProperty) extends SerializerBase[Product](t) {
  override def serialize(value: Product, g: JsonGenerator, p: SerializerProvider) {
    var s: JsonSerializer[AnyRef] = null
    var c: Class[_] = null

    g.writeStartArray()

    for (v <- value.productIterator) {
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

class TupleDeserializer(t: JavaType, ds: Array[JsonDeserializer[AnyRef]]) extends JsonDeserializer[Product] {
  val constructor = findConstructor

  override def deserialize(p: JsonParser, ctx: DeserializationContext): Product = {
    val values = new Array[AnyRef](t.containedTypeCount)

    for (i <- 0 until values.length) {
      p.nextToken
      values(i) = p.getCurrentToken match {
        case VALUE_NULL => ds(i).getNullValue
        case _          => ds(i).deserialize(p, ctx)
      }
    }
    p.nextToken

    constructor.newInstance(values: _*)
  }

  def findConstructor: Constructor[Product] = {
    val specials = Set[Class[_]](classOf[Double], classOf[Int], classOf[Long])

    val ts = (for (i <- 0 until t.containedTypeCount) yield t.containedType(i))

    if (ts.length > 2 || ts.exists(t => !specials.contains(t.getRawClass))) {
      val types = Array.fill(ts.length)(classOf[AnyRef])
      val cls = t.getRawClass.asInstanceOf[Class[Product]]
      return cls.getConstructor(types: _*)
    }

    val name  = new StringBuilder(ts.length + 11)
    val types = new Array[Class[_]](ts.length)

    name.append(t.getRawClass.getName).append("$mc")
    for (i <- 0 until ts.length) {
      name.append(ts(i).getRawClass match {
        case c if c == classOf[Double] => "D"
        case c if c == classOf[Int]    => "I"
        case c if c == classOf[Long]   => "J"
      })
      types(i) = ts(i).getRawClass
    }
    name.append("$sp")

    val cls = Class.forName(name.toString).asInstanceOf[Class[Product]]
    cls.getConstructor(types: _*)
  }
}
