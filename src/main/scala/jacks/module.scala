// Copyright (C) 2011 - Will Glozer.  All rights reserved.

package com.lambdaworks.jacks

import scala.collection._
import scala.collection.generic._
import scala.collection.mutable.PriorityQueue

import org.codehaus.jackson._
import org.codehaus.jackson.map._
import org.codehaus.jackson.map.`type`._
import org.codehaus.jackson.`type`.JavaType

import java.lang.reflect.Method

import tools.scalap.scalax.rules.scalasig.ScalaSig

class ScalaModule extends Module {
  def version       = new Version(1, 9, 0, null)
  def getModuleName = "ScalaModule"

  def setupModule(ctx: Module.SetupContext) {
    ctx.addSerializers(new ScalaSerializers)
    ctx.addDeserializers(new ScalaDeserializers)
  }
}

class ScalaDeserializers extends Deserializers.Base {
  override def findBeanDeserializer(t: JavaType, cfg: DeserializationConfig, p: DeserializerProvider, bd: BeanDescription, bp: BeanProperty): JsonDeserializer[_] = {
    val cls = t.getRawClass

    if (classOf[GenTraversable[_]].isAssignableFrom(cls)) {
      if (classOf[Seq[_]].isAssignableFrom(cls)) {
        val c = companion[GenericCompanion[Seq]](cls)
        val v = p.findValueDeserializer(cfg, t.containedType(0), bp)
        new SeqDeserializer[Any, Seq](c, v)
      } else if (classOf[SortedMap[_, _]].isAssignableFrom(cls)) {
        val c = companion[SortedMapFactory[SortedMap]](cls)
        val o = ordering(t.containedType(0))
        val k = p.findKeyDeserializer(cfg, t.containedType(0), bp)
        val v = p.findValueDeserializer(cfg, t.containedType(1), bp)
        new SortedMapDeserializer[Any, Any](c, o, k, v)
      } else if (classOf[Map[_, _]].isAssignableFrom(cls)) {
        val c = companion[GenMapFactory[Map]](cls)
        val k = p.findKeyDeserializer(cfg, t.containedType(0), bp)
        val v = p.findValueDeserializer(cfg, t.containedType(1), bp)
        new MapDeserializer[Any, Any](c, k, v)
      } else if (classOf[Set[_]].isAssignableFrom(cls)) {
        if (classOf[SortedSet[_]].isAssignableFrom(cls)) {
          val c = companion[SortedSetFactory[SortedSet]](cls)
          val o = ordering(t.containedType(0))
          val v = p.findValueDeserializer(cfg, t.containedType(0), bp)
          new SortedSetDeserializer[Any, SortedSet](c, o, v)
        } else if (classOf[BitSet].isAssignableFrom(cls)) {
          val c = companion[BitSetFactory[BitSet]](cls)
          val t = cfg.getTypeFactory.constructType(classOf[Int])
          val v = p.findValueDeserializer(cfg, t, bp)
          new BitSetDeserializer[BitSet](c, v)
        } else {
          val c = companion[GenericCompanion[Set]](cls)
          val v = p.findValueDeserializer(cfg, t.containedType(0), bp)
          new SeqDeserializer[Any, Set](c, v)
        }
      } else if (classOf[PriorityQueue[_]].isAssignableFrom(cls)) {
        val c = companion[OrderedTraversableFactory[PriorityQueue]](cls)
        val o = ordering(t.containedType(0))
        val v = p.findValueDeserializer(cfg, t.containedType(0), bp)
        new OrderedDeserializer[Any, PriorityQueue](c, o , v)
      } else {
        null
      }
    } else if (classOf[Option[_]].isAssignableFrom(cls)) {
      val v = p.findValueDeserializer(cfg, t.containedType(0), bp)
      new OptionDeserializer(v)
    } else if (classOf[Product].isAssignableFrom(cls) && cls.getName.startsWith("scala.Tuple")) {
      val vs = for (i <- 0 until t.containedTypeCount) yield p.findValueDeserializer(cfg, t.containedType(i), bp)
      new TupleDeserializer(t, vs.toArray)
    } else if (classOf[Product].isAssignableFrom(cls)) {
      ScalaTypeSig(cfg.getTypeFactory, t) match {
        case Some(sts) if sts.isCaseClass =>
          val ds = sts.accessors.map(a => (a.name -> p.findValueDeserializer(cfg, a.`type`, bp)))
          new CaseClassDeserializer(sts.constructor, sts.accessors, ds.toMap)
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
  override def findSerializer(cfg: SerializationConfig, t: JavaType, bd: BeanDescription, bp: BeanProperty): JsonSerializer[_] = {
    val cls = t.getRawClass

    if (classOf[Map[_, _]].isAssignableFrom(cls)) {
      new MapSerializer(t, bp)
    } else if (classOf[Iterable[_]].isAssignableFrom(cls)) {
      var vT = t.containedType(0)
      if (vT == null) vT = cfg.getTypeFactory.constructType(classOf[AnyRef])
      new IterableSerializer(vT, bp)
    } else if (classOf[Option[_]].isAssignableFrom(cls)) {
      new OptionSerializer(t, bp)
    } else if (classOf[Product].isAssignableFrom(cls) && cls.getName.startsWith("scala.Tuple")) {
      new TupleSerializer(t, bp)
    } else if (classOf[Product].isAssignableFrom(cls)) {
      ScalaTypeSig(cfg.getTypeFactory, t) match {
        case Some(sts) if sts.isCaseClass => new CaseClassSerializer(t, sts.accessors, bp)
        case _                            => null
      }
    } else if (classOf[Symbol].isAssignableFrom(cls)) {
      new SymbolSerializer(t, bp)
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
  def constructor = `type`.getRawClass.getDeclaredConstructors.head

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

  def apply(tf: TypeFactory, t: JavaType): Option[ScalaTypeSig] = {
    ScalaSigParser.parse(t.getRawClass) match {
      case Some(sig) => Some(new ScalaTypeSig(tf, t, sig))
      case None      => None
    }
  }

  val types = immutable.Map[String, Class[_]](
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
