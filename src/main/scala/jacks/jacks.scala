// Copyright (C) 2011 - Will Glozer.  All rights reserved.

package com.lambdaworks.jacks

import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.ObjectMapper

import scala.collection.convert.Wrappers.JConcurrentMapWrapper

import java.io._
import java.util.concurrent.ConcurrentHashMap

import reflect.runtime.universe._

trait JacksMapper {
  val mapper = new ObjectMapper
  mapper.registerModule(new ScalaModule)

  def readValue[T: TypeTag](src: Array[Byte]): T = mapper.readValue(src, resolve[T])
  def readValue[T: TypeTag](src: InputStream): T = mapper.readValue(src, resolve[T])
  def readValue[T: TypeTag](src: Reader): T      = mapper.readValue(src, resolve[T])
  def readValue[T: TypeTag](src: String): T      = mapper.readValue(src, resolve[T])

  def writeValue(w: Writer, v: Any)         { mapper.writeValue(w, v) }
  def writeValue(o: OutputStream, v: Any)   { mapper.writeValue(o, v) }
  def writeValueAsString[T: TypeTag](v: T) = writerWithType[T].writeValueAsString(v)

  def writerWithType[T: TypeTag]           = mapper.writerWithType(resolve[T])

  val cache = JConcurrentMapWrapper(new ConcurrentHashMap[Type, JavaType])

  def resolve[T: TypeTag]: JavaType = resolve(typeOf[T])

  def resolve(t: Type): JavaType = cache.getOrElseUpdate(t, {

    val cm = reflect.runtime.currentMirror
    def runtimeClass(t: Type) = cm.runtimeClass(t.erasure)
    val typeArguments = t.typeArgs.map(resolve)

    val tf = mapper.getTypeFactory

    if (t <:< typeOf[Null]) tf.constructType(classOf[Any])
    else if (typeArguments.isEmpty || t <:< typeOf[Array[_]]) tf.constructType(runtimeClass(t))
    else tf.constructParametricType(runtimeClass(t), typeArguments: _*)

  })
}

object JacksMapper extends JacksMapper
