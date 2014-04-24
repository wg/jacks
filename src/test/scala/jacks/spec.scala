// Copyright (C) 2011 - Will Glozer.  All rights reserved.

package com.lambdaworks.jacks

import org.scalacheck._
import org.scalacheck.Prop.forAll

import scala.beans.BeanProperty

object ImmutableCollectionSpec extends ScalaModuleSpec("") {
  import scala.collection.immutable._
  import ImmutableCollections._

  // Seq
  property("Vector[Int]")   = forAll { (c: Vector[Int])   => c == rw(c) }
  property("List[Boolean]") = forAll { (c: List[Boolean]) => c == rw(c) }
  property("Stream[Int]")   = forAll { (c: Stream[Int])   => c == rw(c) }
  property("Queue[Char]")   = forAll { (c: Queue[Char])   => c == rw(c) }

  // Set
  property("Set[Boolean]")  = forAll { (s: Set[Boolean])  => s == rw(s) }
  property("BitSet")        = forAll { (s: BitSet)        => s == rw(s) }
  property("HashSet[Byte]") = forAll { (s: HashSet[Byte]) => s == rw(s) }
  property("ListSet[Char]") = forAll { (s: ListSet[Char]) => s == rw(s) }

  // complex types
  property("List[Bean]")       = forAll { (c: List[Bean])       => c == rw(c) }
  property("List[CaseClass]")  = forAll { (c: List[CaseClass])  => c == rw(c) }
  property("List[(Int, Int)]") = forAll { (c: List[(Int, Int)]) => c == rw(c) }
}

object MutableCollectionSpec extends ScalaModuleSpec("") {
  import scala.collection.mutable._
  import MutableCollections._

  // IndexedSeq, Buffer
  property("ArraySeq[Boolean]")     = forAll { (c: ArraySeq[Boolean])     => c == rw(c) }
  property("ArrayBuffer[Int]")      = forAll { (c: ArrayBuffer[Int])      => c == rw(c) }
  property("ListBuffer[Char]")      = forAll { (c: ListBuffer[Char])      => c == rw(c) }

  // Seq (ArrayStack Builder is broken in Scala 2.9.1)
  property("Stack[Int]")            = forAll { (c: Stack[Int])            => c == rw(c) }
  //property("ArrayStack[Int]")     = forAll { (c: ArrayStack[Int])       => c == rw(c) }
  property("PriorityQueue[Int]")    = forAll { (c: PriorityQueue[Int])    => c.toList == rw(c).toList }

  // LinearSeq
  property("MutableList[Int]")      = forAll { (c: MutableList[Int])      => c == rw(c) }
  property("Queue[Int]")            = forAll { (c: Queue[Int])            => c == rw(c) }

  // Set
  property("Set[Boolean]")        = forAll { (s: Set[Boolean])        => s == rw(s) }
  property("BitSet")              = forAll { (s: BitSet)              => s == rw(s) }
  property("HashSet[Byte]")       = forAll { (s: HashSet[Byte])       => s == rw(s) }
  property("LinkedHashSet[Char]") = forAll { (s: LinkedHashSet[Char]) => s == rw(s) }

  property("null") = List("one", null) == rw(List("one", null))
}

object ImmutableMapSpec extends ScalaModuleSpec("") {
  import scala.collection.immutable._
  import ImmutableCollections._

  property("Map[String,Boolean]") = forAll { (m: Map[String,Boolean]) => m == rw(m) }
  property("Map[String,Byte]")    = forAll { (m: Map[String,Byte])    => m == rw(m) }
  property("Map[String,Char]")    = forAll { (m: Map[String,Char])    => m == rw(m) }
  property("Map[String,Double]")  = forAll { (m: Map[String,Double])  => m == rw(m) }
  property("Map[String,Float]")   = forAll { (m: Map[String,Float])   => m == rw(m) }
  property("Map[String,Int]")     = forAll { (m: Map[String,Int])     => m == rw(m) }
  property("Map[String,Long]")    = forAll { (m: Map[String,Long])    => m == rw(m) }
  property("Map[String,Short]")   = forAll { (m: Map[String,Short])   => m == rw(m) }
  property("Map[String,String]")  = forAll { (m: Map[String,String])  => m == rw(m) }

  property("HashMap[String,Int]") = forAll { (m: HashMap[String,Int]) => m == rw(m) }
  property("ListMap[String,Int]") = forAll { (m: ListMap[String,Int]) => m == rw(m) }

  property("Map[String,null]")    = Map("one" -> null) == rw(Map("one" -> null))

  property("Map[String,List[Int]]") = forAll { (m: Map[String,List[Int]]) => m == rw(m) }
  property("Map[String,(Int,Int)]") = forAll { (m: Map[String,(Int,Int)]) => m == rw(m) }

  property("Map[Map[String,Bean]]") = forAll { (m: Map[String,Bean]) => m == rw(m)  }
}

object MutableMapSpec extends ScalaModuleSpec("") {
  import scala.collection.mutable._
  import MutableCollections._

  property("HashMap[String,Int]")       = forAll { (m: HashMap[String,Int])       => m == rw(m) }
  property("LinkedHashMap[String,Int]") = forAll { (m: LinkedHashMap[String,Int]) => m == rw(m) }
  property("ListMap[String,Int]")       = forAll { (m: ListMap[String,Int])       => m == rw(m) }

  property("WeakHashMap[String,Int]") = forAll {
    (m: HashMap[String,Int]) =>
      val weakMap = WeakHashMap.empty ++ m
      weakMap == rw(weakMap)
  }

  // unsupported
  //property("OpenHashMap[String,Int]")   = forAll { (m: OpenHashMap[String,Int])   => m == rw(m) }
}

object SortedSetSpec extends ScalaModuleSpec("") {
  import scala.collection.immutable.TreeSet
  import ImmutableCollections._

  property("TreeSet[Boolean]")     = forAll { (c: TreeSet[Boolean])     => c == rw(c) }
  property("TreeSet[Byte]")        = forAll { (c: TreeSet[Byte])        => c == rw(c) }
  property("TreeSet[Char]")        = forAll { (c: TreeSet[Char])        => c == rw(c) }
  property("TreeSet[Double]")      = forAll { (c: TreeSet[Double])      => c == rw(c) }
  property("TreeSet[Float]")       = forAll { (c: TreeSet[Float])       => c == rw(c) }
  property("TreeSet[Int]")         = forAll { (c: TreeSet[Int])         => c == rw(c) }
  property("TreeSet[Long]]")       = forAll { (c: TreeSet[Long])        => c == rw(c) }
  property("TreeSet[Short]]")      = forAll { (c: TreeSet[Short])       => c == rw(c) }
  property("TreeSet[String]")      = forAll { (c: TreeSet[String])      => c == rw(c) }
  property("TreeSet[(Int, Int)]")  = forAll { (c: TreeSet[(Int, Int)])  => c == rw(c) }
  property("TreeSet[Option[Int]]") = forAll { (c: TreeSet[Option[Int]]) => c == rw(c) }
}

object SortedMapSpec extends ScalaModuleSpec("") {
  import scala.collection.immutable.TreeMap
  import ImmutableCollections._

  property("TreeMap[String,Int]")  = forAll { (c: TreeMap[String,Int])  => c == rw(c) }
}

object CollectionWrapperSpec extends ScalaModuleSpec("") {
  import scala.collection.JavaConversions._

  property("List  -> JList") = forAll { (l: List[String]) => seqAsJavaList(l) == rw(l, seqAsJavaList(l)) }
  property("JList -> List")  = forAll { (l: List[String]) => l == rw(seqAsJavaList(l), l) }
  property("Set  -> JSet")   = forAll { (s: Set[String])  => setAsJavaSet(s) == rw(s, setAsJavaSet(s)) }
  property("JSet -> Set")    = forAll { (s: Set[String])  => s == rw(setAsJavaSet(s), s) }
  property("Map  -> JMap")   = forAll { (m: Map[String,String]) => mapAsJavaMap(m) == rw(m, mapAsJavaMap(m)) }
  property("JMap -> Map")    = forAll { (m: Map[String,String]) => m == rw(mapAsJavaMap(m), m) }
}

object OptionSpec extends ScalaModuleSpec("Option") {
  import scala.collection._
  import ScalaCollections._

  // root level Options are not supported, Jackson returns null
  property("List")       = forAll { (c: List[Option[String]])        => c == rw(c) }
  property("Map")        = forAll { (c: Map[String, Option[String]]) => c == rw(c) }
  property("Set")        = forAll { (c: Set[Option[String]])         => c == rw(c) }
  property("(Int, Int)") = forAll { (o: Option[(Int, Int)])          => o == rw(o) }
}

object TupleSpec extends ScalaModuleSpec("Tuple") {
  property("(Boolean, Byte)")       = forAll { (t: (Boolean, Byte))       => t == rw(t) }
  property("(Char, Double, Int)")   = forAll { (t: (Char, Double, Int))   => t == rw(t) }
  property("(Float, Long, Short)")  = forAll { (t: (Float, Long, Short))  => t == rw(t) }
  property("(List[String], Bean)")  = forAll { (t: (List[String], Bean))  => t == rw(t) }
  property("(Option[Int], Long)")   = forAll { (t: (Option[Int], Long))   => t == rw(t) }
  property("(Int, Int, Int, Int)")  = forAll { (t: (Int, Int, Int, Int))  => t == rw(t) }
  property("(Symbol, Float)")       = forAll { (t: (Symbol, Float))       => t == rw(t) }
  property("(String)")              = forAll { (t: Tuple1[String])        => t == rw(t) }

  property("(String, Array[Int])") = forAll {
    (t: (String, Array[Int])) => rw(t) match {
      case (s, a) => t._1 == s && java.util.Arrays.equals(t._2, a)
    }
  }

  property("(Int) specialization")         = rw(Tuple1(1)).getClass == Tuple1(1).getClass
  property("(Int, Int) specialization")    = rw((1, 2)).getClass    == (1, 2).getClass
  property("(Double, Int) specialization") = rw((1.0, 2)).getClass  == (1.0, 2).getClass
  property("(Long, Long) specialization")  = rw((1L, 2L)).getClass  == (1L, 2L).getClass
}

object SymbolSpec extends ScalaModuleSpec("Symbol") {
  property("All") = forAll { (s: Symbol) => s == rw(s) }
}

object CaseClassSpec extends ScalaModuleSpec("CaseClass") {
  property("All") = forAll { (cc: CaseClass) => cc == rw(cc) }
}

class Bean(@BeanProperty var name: String, @BeanProperty var value: Int) {
  def this() = this(null, 0)

  override def equals(o: Any): Boolean = o match {
    case b:Bean => (b.name == name && b.value == value)
    case _      => false
  }

  override def hashCode: Int = {
    (if (name != null) name.hashCode else 0) + value
  }

  override def toString = "Bean(name = \"%s\", value = %d)".format(name, value)
}

case class CaseClass(
  boolean: Boolean,
  char:    Char,
  int:     Int,
  bean:    Bean,
  list:    List[Char]
)

abstract class ScalaModuleSpec(name: String) extends Properties(name) {
  import JacksMapper._

  def r[T](s: String)(implicit m: Manifest[T]): T = readValue(s)

  def rw[T](v: T)(implicit m: Manifest[T]): T = {
    val t = resolve(m)
    val s = mapper.writeValueAsString(v)
    mapper.readValue(s, t)
  }

  def rw[T,U](v: T, v2: U)(implicit m: Manifest[U]): U = {
    val u = resolve(m)
    val s = mapper.writeValueAsString(v)
    mapper.readValue(s, u)
  }

  def beanGen = for {
    name  <- Arbitrary.arbitrary[String]
    value <- Arbitrary.arbitrary[Int]
  } yield (new Bean(name, value))

  def caseClassGen = for {
    boolean <- Arbitrary.arbitrary[Boolean]
    char    <- Arbitrary.arbitrary[Char]
    int     <- Arbitrary.arbitrary[Int]
    bean    <- Arbitrary.arbitrary[Bean]
    list    <- Arbitrary.arbitrary[List[Char]]
  } yield CaseClass(boolean, char, int, bean, list)

  implicit def arbitraryBean: Arbitrary[Bean] = Arbitrary { beanGen }

  implicit def arbitraryCaseClass: Arbitrary[CaseClass] = Arbitrary { caseClassGen }

  implicit def arbitraryOption[T](implicit a: Arbitrary[T]) = Arbitrary {
    val genSome = for (v <- Arbitrary.arbitrary[T]) yield Some(v)
    Gen.oneOf(None, genSome)
  }

  implicit def arbitraryTuple1[T](implicit a: Arbitrary[T]): Arbitrary[Tuple1[T]] = Arbitrary {
    for (v <- Arbitrary.arbitrary[T]) yield Tuple1(v)
  }

  implicit def arbitrarySymbol: Arbitrary[Symbol] = Arbitrary {
    for (s <- Arbitrary.arbitrary[String]) yield Symbol(s)
  }
}

trait ArbitraryCollections {
  import scala.collection.generic._
  import scala.collection.{GenMap, GenMapLike}

  def positiveIntStream: Arbitrary[Stream[Int]] = Arbitrary {
    Gen.containerOf[Stream, Int](Gen.chooseNum(0, Short.MaxValue))
  }

  def arbitrarySeq[T: Arbitrary, C[T] <: Traversable[T]](c: GenericCompanion[C]): Arbitrary[C[T]] =
    Arbitrary {
      for (seq <- Arbitrary.arbitrary[Stream[T]]) yield c(seq: _*)
    }

  def arbitraryMap[K: Arbitrary, V: Arbitrary, C[K, V] <: GenMap[K, V] with GenMapLike[K, V, C[K, V]]]
    (f: GenMapFactory[C]): Arbitrary[C[K, V]] = Arbitrary {
      for (seq <- Arbitrary.arbitrary[Stream[(K, V)]]) yield f(seq: _*)
  }
}

object ScalaCollections extends ArbitraryCollections {
  import scala.collection._

  implicit def arbitraryMap[K: Arbitrary, V: Arbitrary]: Arbitrary[Map[K, V]] = arbitraryMap[K, V, Map](Map)
}

object ImmutableCollections extends ArbitraryCollections {
  import scala.collection.immutable._

  implicit def arbitraryVector[T: Arbitrary]: Arbitrary[Vector[T]] = arbitrarySeq[T, Vector](Vector)
  implicit def arbitraryList[T: Arbitrary]: Arbitrary[List[T]] = arbitrarySeq[T, List](List)
  implicit def arbitraryStream[T: Arbitrary]: Arbitrary[Stream[T]] = arbitrarySeq[T, Stream](Stream)
  implicit def arbitraryQueue[T: Arbitrary]: Arbitrary[Queue[T]] = arbitrarySeq[T, Queue](Queue)

  implicit def arbitraryHashMap[K: Arbitrary, V: Arbitrary]: Arbitrary[HashMap[K, V]] = arbitraryMap[K, V, HashMap](HashMap)
  implicit def arbitraryListMap[K: Arbitrary, V: Arbitrary]: Arbitrary[ListMap[K, V]] = arbitraryMap[K, V, ListMap](ListMap)

  implicit def arbitraryTreeMap[K, V](implicit ak: Arbitrary[K], av: Arbitrary[V], o: Ordering[K]):
    Arbitrary[TreeMap[K, V]] = Arbitrary {
      for (seq <- Arbitrary.arbitrary[Stream[(K, V)]]) yield TreeMap(seq:_*)
  }

  implicit def arbitraryHashSet[T: Arbitrary]: Arbitrary[HashSet[T]] = arbitrarySeq[T, HashSet](HashSet)
  implicit def arbitraryListSet[T: Arbitrary]: Arbitrary[ListSet[T]] = arbitrarySeq[T, ListSet](ListSet)

  implicit def arbitraryTreeSet[T](implicit a: Arbitrary[T], o: Ordering[T]):
    Arbitrary[TreeSet[T]] = Arbitrary {
      for (seq <- Arbitrary.arbitrary[Stream[T]]) yield TreeSet(seq: _*)
  }

  implicit def arbitraryBitSet: Arbitrary[BitSet] = Arbitrary {
    for (seq <- positiveIntStream.arbitrary) yield BitSet(seq: _*)
  }
}

object MutableCollections extends ArbitraryCollections {
  import scala.collection.mutable._

  implicit def arbitraryArrayBuffer[T: Arbitrary]: Arbitrary[ArrayBuffer[T]] = arbitrarySeq[T, ArrayBuffer](ArrayBuffer)
  implicit def arbitraryArraySeq[T: Arbitrary]: Arbitrary[ArraySeq[T]] = arbitrarySeq[T, ArraySeq](ArraySeq)
  implicit def arbitraryArrayStack[T: Arbitrary]: Arbitrary[ArrayStack[T]] = arbitrarySeq[T, ArrayStack](ArrayStack)
  implicit def arbitraryListBuffer[T: Arbitrary]: Arbitrary[ListBuffer[T]] = arbitrarySeq[T, ListBuffer](ListBuffer)
  implicit def arbitraryMutableList[T: Arbitrary]: Arbitrary[MutableList[T]] = arbitrarySeq[T, MutableList](MutableList)
  implicit def arbitraryQueue[T: Arbitrary]: Arbitrary[Queue[T]] = arbitrarySeq[T, Queue](Queue)
  implicit def arbitraryStack[T: Arbitrary]: Arbitrary[Stack[T]] = arbitrarySeq[T, Stack](Stack)

  implicit def arbitraryPriorityQueue[T](implicit a: Arbitrary[T], o: Ordering[T]): Arbitrary[PriorityQueue[T]] =
    Arbitrary {
      for (seq <- Arbitrary.arbitrary[Stream[T]]) yield PriorityQueue(seq: _*)
    }

  implicit def arbitraryHashMap[K: Arbitrary, V: Arbitrary]: Arbitrary[HashMap[K, V]] = arbitraryMap[K, V, HashMap](HashMap)
  implicit def arbitraryLinkedHashMap[K: Arbitrary, V: Arbitrary]: Arbitrary[LinkedHashMap[K, V]] =
    arbitraryMap[K, V, LinkedHashMap](LinkedHashMap)
  implicit def arbitraryListMap[K: Arbitrary, V: Arbitrary]: Arbitrary[ListMap[K, V]] = arbitraryMap[K, V, ListMap](ListMap)

  implicit def arbitraryHashSet[T: Arbitrary]: Arbitrary[HashSet[T]] = arbitrarySeq[T, HashSet](HashSet)
  implicit def arbitraryLinkedHashSet[T: Arbitrary]: Arbitrary[LinkedHashSet[T]] = arbitrarySeq[T, LinkedHashSet](LinkedHashSet)

  implicit def arbitraryBitSet: Arbitrary[BitSet] = Arbitrary {
    for (seq <- positiveIntStream.arbitrary) yield BitSet(seq: _*)
  }
}
