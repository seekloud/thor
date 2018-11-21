package org.seekloud.thor.front.utils.byteObject


import org.seekloud.thor.shared.ptcl.util.MiddleBuffer
import shapeless.labelled.FieldType
import shapeless.{:+:, ::, CNil, Coproduct, HList, HNil, Inl, Inr, LabelledGeneric, Lazy, Witness}

/**
  * User: Taoz
  * Date: 7/15/2018
  * Time: 8:43 AM
  */
package object encoder {

  trait BytesEncoder[A] {
    def encode(a: A, buffer: MiddleBuffer): MiddleBuffer
  }

  object BytesEncoder {
    //summoner
    def apply[A](implicit enc: BytesEncoder[A]): BytesEncoder[A] = enc

    //constructor
    def instance[A](func: (A, MiddleBuffer) => MiddleBuffer): BytesEncoder[A] = {
      new BytesEncoder[A] {
        override def encode(a: A, buffer: MiddleBuffer): MiddleBuffer = {
          func(a, buffer)
        }
      }
    }


    implicit val intEncoder: BytesEncoder[Int] = instance[Int] { (i, buffer) => buffer.putInt(i) }
    implicit val floatEncoder: BytesEncoder[Float] = instance[Float] { (f, buffer) => buffer.putFloat(f) }
    implicit val doubleEncoder: BytesEncoder[Double] = instance[Double] { (d, buffer) => buffer.putDouble(d) }
    implicit val stringEncoder: BytesEncoder[String] = instance[String] { (s, buffer) => buffer.putString(s) }
    implicit val booleanEncoder: BytesEncoder[Boolean] = instance[Boolean] { (b, buffer) => buffer.putBoolean(b)}
    implicit val byteEncoder: BytesEncoder[Byte] = instance[Byte] { (b, buffer) => buffer.putByte(b) }
    implicit val charEncoder: BytesEncoder[Char] = instance[Char] { (c, buffer) => buffer.putChar(c)}
    implicit val shortEncoder: BytesEncoder[Short] = instance[Short] { (s, buffer) => buffer.putShort(s) }
    implicit val longEncoder: BytesEncoder[Long] = instance[Long] { (l, buffer) => buffer.putLong(l) }


    implicit def seqEncoder[A](implicit enc: BytesEncoder[A]): BytesEncoder[Seq[A]] = {
      instance { (seq, buffer) =>
        buffer.putInt(seq.length)
        seq.foreach { item => enc.encode(item, buffer) }
        buffer
      }
    }


    implicit def listEncoder[A](implicit enc: BytesEncoder[A]): BytesEncoder[List[A]] = {
      instance { (ls, buffer) =>
        buffer.putInt(ls.length)
        ls.foreach { item => enc.encode(item, buffer) }
        buffer
      }
    }


    implicit def arrayEncoder[A](implicit enc: BytesEncoder[A]): BytesEncoder[Array[A]] = {
      instance { (arr, buffer) =>
        buffer.putInt(arr.length)
        arr.foreach { item => enc.encode(item, buffer) }
        buffer
      }
    }

    implicit def optionEncoder[A](implicit enc: BytesEncoder[A]): BytesEncoder[Option[A]] = {
      instance { (option, buffer) =>
        buffer.putInt(if (option.isDefined) 1 else 0)
        option.foreach { item => enc.encode(item, buffer) }
        buffer
      }
    }

    implicit def mapEncoder[K, V](
      implicit kEnc: BytesEncoder[K], vEnc: BytesEncoder[V]
    ): BytesEncoder[Map[K, V]] = {
      instance { (map, buffer) =>
        buffer.putInt(map.size)
        map.foreach { case (key, value) =>
          kEnc.encode(key, buffer)
          vEnc.encode(value, buffer)
        }
        buffer
      }
    }
    /*

        implicit val intArrayEncoder: Encoder[Array[Int]] = {
          instance[Array[Int]] { (arr, buffer) => buffer.putIntArray(arr) }
        }
        implicit val floatArrayEncoder: Encoder[Array[Float]] = instance[Array[Float]] { (arr, buffer) => buffer.putFloatArray(arr) }
        implicit val stringArrayEncoder: Encoder[Array[String]] = instance[Array[String]] { (arr, buffer) => buffer.putStringArray(arr) }
    */


    implicit val hNilEncoder: BytesEncoder[HNil] = instance[HNil] { (a, buffer) =>
      //do nothing.
      buffer
    }

    implicit val cNilEncoder: BytesEncoder[CNil] = instance[CNil] { (a, buffer) =>
      throw new Exception("cNilEncoder should be never called.")
    }

    implicit def hListEncoder[K <: Symbol, H, T <: HList](
      implicit
      witness: Witness.Aux[K],
      hEncoder: Lazy[BytesEncoder[H]],
      tEncoder: BytesEncoder[T]
    ): BytesEncoder[FieldType[K, H] :: T] = {
      instance { (hList, buffer) =>
//        val name = witness.value.name
//        val value = witness.value
//        println(s"hListEncoder, process. name=$name value=$value")
//        println(s"h: ${hList.head.getClass}")
//        println(s"t: ${hList.tail.getClass}")
        hEncoder.value.encode(hList.head, buffer)
        tEncoder.encode(hList.tail, buffer)
        buffer
      }
    }

    implicit def coproductEncoder[K <: Symbol, H, T <: Coproduct](
      implicit
      witness: Witness.Aux[K],
      hEncoder: Lazy[BytesEncoder[H]],
      tEncoder: BytesEncoder[T]
    ): BytesEncoder[FieldType[K, H] :+: T] = {
      instance { (coproduct, buffer) =>
        val name = witness.value.name
//        val value = witness.value
//        println(s"coproductEncoder, process. name=$name value=$value")
        coproduct match {
          case Inl(h) =>
//            println(s"in left.")
//            println(s"h: ${h.getClass}")
            buffer.putString(name)
            hEncoder.value.encode(h, buffer)
          case Inr(t) =>
//            println(s"in right.")
//            println(s"t: ${t.getClass}")
            tEncoder.encode(t, buffer)
        }
      }
    }


    implicit def genericEncoder[A, R](
      implicit
      generic: LabelledGeneric.Aux[A, R],
      enc: Lazy[BytesEncoder[R]]
    ): BytesEncoder[A] = {
      instance { (a, buffer) =>
        enc.value.encode(generic.to(a), buffer)
      }
    }
  }


}
