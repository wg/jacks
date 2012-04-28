// Copyright (C) 2011 - Will Glozer.  All rights reserved.

package com.lambdaworks.jacks

import scala.collection._
import scala.collection.generic._
import scala.collection.mutable.PriorityQueue

import com.fasterxml.jackson.core._
import com.fasterxml.jackson.databind._
import com.fasterxml.jackson.databind.deser._
import com.fasterxml.jackson.databind.ser._
import com.fasterxml.jackson.databind.`type`._

import java.lang.reflect.{Constructor, Method}

import tools.scalap.scalax.rules.scalasig.ScalaSig

class ScalaModule extends Module {
  def version       = new Version(2, 0, 1, null, "com.lambdaworks", "jacks")
  def getModuleName = "ScalaModule"

  def setupModule(ctx: Module.SetupContext) {
    ctx.addSerializers(new ScalaSerializers)
    ctx.addDeserializers(new ScalaDeserializers)
  }
}

class ScalaDeserializers extends Deserializers.Base {
  override def findBeanDeserializer(t: JavaType, cfg: DeserializationConfig, bd: BeanDescription): JsonDeserializer[_] = {
    val cls = t.getRawClass

    if (classOf[GenTraversable[_]].isAssignableFrom(cls)) {
      if (classOf[GenSeq[_]].isAssignableFrom(cls)) {
        val c = companion[GenericCompanion[GenSeq]](cls)
        new SeqDeserializer[Any, GenSeq](c, t.containedType(0))
      } else if (classOf[SortedMap[_, _]].isAssignableFrom(cls)) {
        val c = companion[SortedMapFactory[SortedMap]](cls)
        val o = ordering(t.containedType(0))
        new SortedMapDeserializer[Any, Any](c, o, t.containedType(0), t.containedType(1))
      } else if (classOf[GenMap[_, _]].isAssignableFrom(cls)) {
        val c = companion[GenMapFactory[GenMap]](cls)
        new MapDeserializer[Any, Any](c, t.containedType(0), t.containedType(1))
      } else if (classOf[GenSet[_]].isAssignableFrom(cls)) {
        if (classOf[SortedSet[_]].isAssignableFrom(cls)) {
          val c = companion[SortedSetFactory[SortedSet]](cls)
          val o = ordering(t.containedType(0))
          new SortedSetDeserializer[Any, SortedSet](c, o, t.containedType(0))
        } else if (classOf[BitSet].isAssignableFrom(cls)) {
          val c = companion[BitSetFactory[BitSet]](cls)
          val t = cfg.getTypeFactory.constructType(classOf[Int])
          new BitSetDeserializer[BitSet](c, t)
        } else {
          val c = companion[GenericCompanion[GenSet]](cls)
          new SeqDeserializer[Any, GenSet](c, t.containedType(0))
        }
      } else if (classOf[PriorityQueue[_]].isAssignableFrom(cls)) {
        val c = companion[OrderedTraversableFactory[PriorityQueue]](cls)
        val o = ordering(t.containedType(0))
        new OrderedDeserializer[Any, PriorityQueue](c, o, t.containedType(0))
      } else {
        null
      }
    } else if (classOf[Option[_]].isAssignableFrom(cls)) {
      new OptionDeserializer(t.containedType(0))
    } else if (classOf[Product].isAssignableFrom(cls) && cls.getName.startsWith("scala.Tuple")) {
      new TupleDeserializer(t)
    } else if (classOf[Product].isAssignableFrom(cls)) {
      ScalaTypeSig(cfg.getTypeFactory, t) match {
        case Some(sts) if sts.isCaseClass =>
          new CaseClassDeserializer(sts.constructor, sts.accessors)
        case _ =>
          null
      }
    } else if (classOf[Symbol].isAssignableFrom(cls)) {
      new SymbolDeserializer
    } else {
      null
    }
  }

  def companion[T](cls: Class[_]): T = {
    Class.forName(cls.getName + "$").getField("MODULE$").get(null).asInstanceOf[T]
  }

  lazy val orderings = Map[Class[_], Ordering[_]](
    classOf[Boolean]    -> Ordering.Boolean,
    classOf[Byte]       -> Ordering.Byte,
    classOf[Char]       -> Ordering.Char,
    classOf[Double]     -> Ordering.Double,
    classOf[Int]        -> Ordering.Int,
    classOf[Float]      -> Ordering.Float,
    classOf[Long]       -> Ordering.Long,
    classOf[Short]      -> Ordering.Short,
    classOf[String]     -> Ordering.String,
    classOf[BigInt]     -> Ordering.BigInt,
    classOf[BigDecimal] -> Ordering.BigDecimal)

  def ordering(t: JavaType): Ordering[Any] = {
    val cls = t.getRawClass
    orderings.getOrElse(cls, {
      val orderings = for (i <- 0 until t.containedTypeCount) yield ordering(t.containedType(0))
      val params = Array.fill(orderings.length)(classOf[Ordering[_]])
      val method = Ordering.getClass.getMethod(cls.getSimpleName, params:_*)
      method.invoke(Ordering, orderings:_*)
    }).asInstanceOf[Ordering[Any]]
  }
}

class ScalaSerializers extends Serializers.Base {
  override def findSerializer(cfg: SerializationConfig, t: JavaType, bd: BeanDescription): JsonSerializer[_] = {
    val cls = t.getRawClass

    if (classOf[GenMap[_, _]].isAssignableFrom(cls)) {
      new MapSerializer(t)
    } else if (classOf[GenIterable[_]].isAssignableFrom(cls)) {
      var vT = t.containedType(0)
      if (vT == null) vT = cfg.getTypeFactory.constructType(classOf[AnyRef])
      new IterableSerializer(vT)
    } else if (classOf[Option[_]].isAssignableFrom(cls)) {
      new OptionSerializer(t)
    } else if (classOf[Product].isAssignableFrom(cls) && cls.getName.startsWith("scala.Tuple")) {
      new TupleSerializer(t)
    } else if (classOf[Product].isAssignableFrom(cls)) {
      ScalaTypeSig(cfg.getTypeFactory, t) match {
        case Some(sts) if sts.isCaseClass => new CaseClassSerializer(t, sts.accessors)
        case _                            => null
      }
    } else if (classOf[Symbol].isAssignableFrom(cls)) {
      new SymbolSerializer(t)
    } else {
      null
    }
  }
}

case class Accessor(
  name:    String,
  `type`:  JavaType,
  default: Option[Method]
)

class ScalaTypeSig(val tf: TypeFactory, val `type`: JavaType, val sig: ScalaSig) {
  import tools.scalap.scalax.rules.scalasig.{Method => _,  _}

  val cls = sig.topLevelClasses.head.asInstanceOf[ClassSymbol]

  def isCaseClass = cls.isCase
  def constructor: Constructor[_] = {
    val types = accessors.map(_.`type`.getRawClass)
    `type`.getRawClass.getDeclaredConstructors.find { c =>
      val pairs = c.getParameterTypes.zip(types)
      pairs.length == types.length && pairs.forall {
        case (a, b) => a.isAssignableFrom(b)
      }
    }.get
  }

  def accessors: Array[Accessor] = {
    var list  = collection.mutable.ListBuffer[Accessor]()
    var index = 0

    for (c <- cls.children if c.isCaseAccessor && !c.isPrivate) {
      val sym = c.asInstanceOf[MethodSymbol]
      index += 1
      list  += Accessor(sym.name, resolve(sym.infoType), default(index))
    }

    list.toArray
  }

  def default(index: Int): Option[Method] = try {
    val m = `type`.getRawClass.getDeclaredMethod("init$default$%d".format(index))
    Some(m)
  } catch {
    case e:NoSuchMethodException => None
  }

  lazy val contained = (0 until `type`.containedTypeCount).map {
    i => (`type`.containedTypeName(i) -> `type`.containedType(i))
  }.toMap

  def resolve(t: Type): JavaType = t match {
    case NullaryMethodType(t2) =>
      resolve(t2)
    case TypeRefType(_, TypeSymbol(s), Nil) =>
      contained(s.name)
    case TypeRefType(_, s, Nil) =>
      tf.constructType(ScalaTypeSig.resolve(s))
    case TypeRefType(_, s, a :: Nil) if s.path == "scala.Array" =>
      ArrayType.construct(resolve(a), null, null)
    case TypeRefType(_, s, args) =>
      val params = args.map(resolve(_))
      tf.constructParametricType(ScalaTypeSig.resolve(s), params: _ *)
  }
}

object ScalaTypeSig {
  import tools.scalap.scalax.rules.scalasig.{ScalaSigParser, Symbol}
  import scala.collection.immutable._

  def apply(tf: TypeFactory, t: JavaType): Option[ScalaTypeSig] = {
    ScalaSigParser.parse(t.getRawClass) match {
      case Some(sig) => Some(new ScalaTypeSig(tf, t, sig))
      case None      => None
    }
  }

  val types = Map[String, Class[_]](
    "scala.Boolean"       -> classOf[Boolean],
    "scala.Byte"          -> classOf[Byte],
    "scala.Char"          -> classOf[Char],
    "scala.Double"        -> classOf[Double],
    "scala.Int"           -> classOf[Int],
    "scala.Float"         -> classOf[Float],
    "scala.Long"          -> classOf[Long],
    "scala.Short"         -> classOf[Short],
    "scala.AnyRef"        -> classOf[AnyRef],
    "scala.Predef.Map"    -> classOf[Map[_, _]],
    "scala.Predef.Set"    -> classOf[Set[_]],
    "scala.Predef.String" -> classOf[String],
    "scala.package.List"  -> classOf[List[_]]
  ).withDefault(Class.forName(_))

  def resolve(s: Symbol) = types(s.path)
}
