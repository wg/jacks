// Copyright (C) 2011 - Will Glozer.  All rights reserved.

package com.lambdaworks.jacks

import org.codehaus.jackson._
import org.codehaus.jackson.map._
import org.codehaus.jackson.map.ser.std.SerializerBase
import org.codehaus.jackson.`type`.JavaType

class SymbolSerializer(t: JavaType, bp: BeanProperty) extends SerializerBase[Symbol](t) {
  override def serialize(value: Symbol, g: JsonGenerator, p: SerializerProvider) {
    g.writeString(value.name)
  }
}

class SymbolDeserializer extends JsonDeserializer[Symbol] {
  override def deserialize(p: JsonParser, ctx: DeserializationContext) = Symbol(p.getText)
}
