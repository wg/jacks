// Copyright (C) 2011 - Will Glozer.  All rights reserved.

package com.lambdaworks.jacks

import com.fasterxml.jackson.core._
import com.fasterxml.jackson.databind._
import com.fasterxml.jackson.databind.ser.std.StdSerializer

class SymbolSerializer(t: JavaType) extends StdSerializer[Symbol](t) {
  override def serialize(value: Symbol, g: JsonGenerator, p: SerializerProvider) {
    g.writeString(value.name)
  }
}

class SymbolDeserializer extends JsonDeserializer[Symbol] {
  override def deserialize(p: JsonParser, ctx: DeserializationContext) = Symbol(p.getText)
}
