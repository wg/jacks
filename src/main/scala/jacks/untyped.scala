// Copyright (C) 2012 - Will Glozer.  All rights reserved.

package com.lambdaworks.jacks

import com.fasterxml.jackson.core._
import com.fasterxml.jackson.databind._
import com.fasterxml.jackson.databind.deser.std.{UntypedObjectDeserializer => BaseUntypedObjectDeserializer}

class UntypedObjectDeserializer(cfg: DeserializationConfig) extends BaseUntypedObjectDeserializer {
  val o = cfg.getTypeFactory.constructParametrizedType(classOf[Map[_, _]], classOf[Map[_, _]], classOf[String], classOf[AnyRef])
  val a = cfg.getTypeFactory.constructParametrizedType(classOf[List[_]], classOf[List[_]], classOf[AnyRef])

  override def mapArray(p: JsonParser, ctx: DeserializationContext): AnyRef = {
    val d = ctx.findContextualValueDeserializer(a, null)
    ctx.isEnabled(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY) match {
      case true  => mapArrayToArray(p, ctx)
      case false => d.deserialize(p, ctx)
    }
  }

  override def mapObject(p: JsonParser, ctx: DeserializationContext): AnyRef = {
    val d = ctx.findContextualValueDeserializer(o, null)
    d.deserialize(p, ctx)
  }
}
