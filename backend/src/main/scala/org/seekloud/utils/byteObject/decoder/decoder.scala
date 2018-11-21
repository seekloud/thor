package org.seekloud.utils.byteObject

import org.seekloud.thor.shared.ptcl.util.MiddleBuffer
import shapeless.labelled.{FieldType, field}
import shapeless.{:+:, ::, CNil, Coproduct, HList, HNil, Inl, Inr, LabelledGeneric, Lazy, Witness}

import scala.reflect.ClassTag
import scala.util.Try

/**
  * User: Taoz
  * Date: 7/15/2018
  * Time: 8:44 AM
  */
package object decoder {


  sealed abstract class DecoderFailure(val message: String) {
    override def toString = s"DecoderFailure($message)"
  }

  final object DecoderFailure {
    def apply(message: String): DecoderFailure = new DecoderFailure(message) {}

    def invalidType(clazz: String, value: String) =
      DecoderFailure(s"$value's type is not $clazz")

    def missingField(field: String, value: String) =
      DecoderFailure(s"$field is missing in $value")
  }


  trait BytesDecoder[A] {
    def decode(buffer: MiddleBuffer): Either[DecoderFailure, A]
  }

  object BytesDecoder {
    //summoner
    def apply[A](implicit dec: BytesDecoder[A]): BytesDecoder[A] = dec

    //constructor
    def instance[A](func: MiddleBuffer => Either[DecoderFailure, A]): BytesDecoder[A] = {
      new BytesDecoder[A] {
        override def decode(buffer: MiddleBuffer): Either[DecoderFailure, A] = {
          func(buffer)
        }
      }
    }


    implicit def genericDecoder[A, R](
      implicit
      gen: LabelledGeneric.Aux[A, R],
      dec: Lazy[BytesDecoder[R]]
    ): BytesDecoder[A] = {
      instance[A] { buffer =>
        for {
          r <- dec.value.decode(buffer).right
        } yield gen.from(r)
      }
    }

    implicit def hListDecoder[K <: Symbol, H, T <: HList](
      implicit
      witness: Witness.Aux[K],
      hDecoder: Lazy[BytesDecoder[H]],
      tDecoder: BytesDecoder[T]
    ): BytesDecoder[FieldType[K, H] :: T] = {
      instance { buffer =>
        for {
          h <- hDecoder.value.decode(buffer).right
          t <- tDecoder.decode(buffer).right
        } yield {
          //        val name = witness.value.name
          //        val value = witness.value
          //        println(s"hListDecoder, process. name=$name value=$value")
          //        println(s"h:$h, h.getClass=${h.getClass}")
          //        println(s"t:$t, t.getClass=${t.getClass}")
          field[K](h) :: t
        }
      }
    }


    trait CoproductTypeBytesDecoder[A] extends BytesDecoder[A] {
      def decodeCoproduct(buffer: MiddleBuffer, nameOption: Option[String]): Either[DecoderFailure, A]

      override def decode(buffer: MiddleBuffer): Either[DecoderFailure, A] = decodeCoproduct(buffer, None)
    }

    implicit def coproductDecoder[K <: Symbol, H, T <: Coproduct](
      implicit
      witness: Witness.Aux[K],
      hDecoder: Lazy[BytesDecoder[H]],
      tDecoder: CoproductTypeBytesDecoder[T]
    ): CoproductTypeBytesDecoder[FieldType[K, H] :+: T] = {
      new CoproductTypeBytesDecoder[FieldType[K, H] :+: T] {
        override def decodeCoproduct(
          buffer: MiddleBuffer,
          nameOption: Option[String]
        ): Either[DecoderFailure, FieldType[K, H] :+: T] = {
          val nameInWitness = witness.value.name
          val value = witness.value
          for {
            cName <- wrapTry {
              nameOption match {
                case Some(name) => name
                case None => buffer.getString()
              }
            }
            rst <-
              if (cName == nameInWitness) {
                for {h <- hDecoder.value.decode(buffer).right} yield Inl(field[K](h))
              } else {
                for {t <- tDecoder.decodeCoproduct(buffer, Some(cName)).right} yield Inr(t)
              }
          } yield rst
        }
      }
    }


    implicit val hNilInstance = instance[HNil] { _ =>
      //do nothing.
      Right(HNil)
    }

    implicit val cNilInstance = {
      new CoproductTypeBytesDecoder[CNil] {
        override def decodeCoproduct(
          buffer: MiddleBuffer,
          nameOption: Option[String]
        ): Either[DecoderFailure, CNil] = {
          Left(DecoderFailure("it should never get to cNilInstance."))
        }
      }
    }

    //instance[CNil] { buffer => throw new Exception("it should never get to cNilInstance.") }

    private def wrapTry[T](func: => T): Either[DecoderFailure, T] = {
      val t = Try(func)
      t.toEither.left.map(e => DecoderFailure(e.getClass + ":" + e.getMessage))
    }

    implicit val intInstance = instance[Int] { buffer => wrapTry(buffer.getInt()) }
    implicit val floatInstance = instance[Float](buffer => wrapTry(buffer.getFloat()))
    implicit val doubleInstance = instance[Double](buffer => wrapTry(buffer.getDouble()))
    implicit val stringInstance = instance[String](buffer => wrapTry(buffer.getString()))
    implicit val booleanInstance = instance[Boolean](buffer => wrapTry(buffer.getBoolean()))
    implicit val charInstance = instance[Char](buffer => wrapTry(buffer.getChar()))
    implicit val byteInstance = instance[Byte](buffer => wrapTry(buffer.getByte()))
    implicit val shortInstance = instance[Short](buffer => wrapTry(buffer.getShort()))
    implicit val longInstance = instance[Long](buffer => wrapTry(buffer.getLong()))


    private def readToArray[A](
      buffer: MiddleBuffer, len: Int, dec: BytesDecoder[A]
    )(implicit m: ClassTag[A]): Either[DecoderFailure, Array[A]] = {
      wrapTry {
        val arr = new Array[A](len)
        var c = 0
        while (c < len) {
          arr(c) = dec.decode(buffer).right.get
          c += 1
        }
        arr
      }
    }

    implicit def seqDecoder[A](implicit dec: BytesDecoder[A], m: ClassTag[A]): BytesDecoder[Seq[A]] = {
      instance[Seq[A]] { buffer =>
        val len = buffer.getInt()
        readToArray(buffer, len, dec).right.map(_.toSeq)
      }
    }


    implicit def listDecoder[A](implicit dec: BytesDecoder[A], m: ClassTag[A]): BytesDecoder[List[A]] = {
      instance[List[A]] { buffer =>
        val len = buffer.getInt()
        readToArray(buffer, len, dec).right.map(_.toList)
      }
    }


    implicit def arrayDecoder[A](implicit dec: BytesDecoder[A], m: ClassTag[A]): BytesDecoder[Array[A]] = {
      instance[Array[A]] { buffer =>
        val len = buffer.getInt()
        readToArray(buffer, len, dec)
      }
    }

    implicit def optionDecoder[A](implicit dec: BytesDecoder[A]): BytesDecoder[Option[A]] = {
      instance[Option[A]] { buffer =>
        val len = buffer.getInt()
        if (len == 1) {
          dec.decode(buffer).right.map(b => Some(b))
        } else {
          Right(None)
        }
      }
    }

    implicit def mapDecoder[K, V](
      implicit
      kDec: BytesDecoder[K],
      vDec: BytesDecoder[V],
      c1: ClassTag[K],
      c2: ClassTag[V]
    ): BytesDecoder[Map[K, V]] = {
      instance { buffer =>
        wrapTry {
          val len = buffer.getInt()
          val kArr = new Array[K](len)
          val vArr = new Array[V](len)
          var c = 0
          while (c < len) {
            kArr(c) = kDec.decode(buffer).right.get
            vArr(c) = vDec.decode(buffer).right.get
            c += 1
          }
          kArr.zip(vArr).toMap
        }
      }
    }
  }
}
