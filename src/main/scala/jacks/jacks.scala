// Copyright (C) 2011 - Will Glozer.  All rights reserved.

package com.lambdaworks.jacks

import org.codehaus.jackson.`type`.JavaType
import org.codehaus.jackson.map.ObjectMapper

import scala.collection.JavaConversions.JConcurrentMapWrapper

import java.io._
import java.util.concurrent.ConcurrentHashMap

trait JacksMapper {
  val mapper = new ObjectMapper
  mapper.registerModule(new ScalaModule)

  def readValue[T: Manifest](src: Array[Byte]): T = mapper.readValue(src, resolve)
  def readValue[T: Manifest](src: InputStream): T = mapper.readValue(src, resolve)
  def readValue[T: Manifest](src: Reader): T      = mapper.readValue(src, resolve)
  def readValue[T: Manifest](src: String): T      = mapper.readValue(src, resolve)

  def writeValue(w: Writer, v: Any)         { mapper.writeValue(w, v) }
  def writeValue(o: OutputStream, v: Any)   { mapper.writeValue(o, v) }
  def writeValueAsString[T: Manifest](v: T) = writerWithType.writeValueAsString(v)

  def writerWithType[T: Manifest]           = mapper.writerWithType(resolve)

  val cache = JConcurrentMapWrapper(new ConcurrentHashMap[Manifest[_], JavaType])

  def resolve(implicit m: Manifest[_]): JavaType = cache.getOrElseUpdate(m, {
    def params = m.typeArguments.map(resolve(_))
    val tf = mapper.getTypeFactory
    m.typeArguments.isEmpty match {
      case true  => tf.constructType(m.erasure)
      case false => tf.constructParametricType(m.erasure, params: _*)
    }
  })
}

object JacksMapper extends JacksMapper