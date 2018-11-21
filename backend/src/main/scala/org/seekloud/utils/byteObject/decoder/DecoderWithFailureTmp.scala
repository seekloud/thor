package org.seekloud.utils.byteObject.decoder

import org.seekloud.thor.shared.ptcl.util.MiddleBuffer
import shapeless.labelled.{FieldType, field}
import shapeless.{:+:, ::, CNil, Coproduct, HList, HNil, Inl, Inr, LabelledGeneric, Lazy, Witness}

import scala.reflect.ClassTag
import scala.util.Try

/**
  * User: Taoz
  * Date: 7/16/2018
  * Time: 1:32 PM
  */
object DecoderWithFailureTmp {

  sealed abstract class DecoderFailureTmp(val message: String) {
    override def toString = s"DecoderFailure($message)"
  }

  final object DecoderFailureTmp {
    def apply(message: String): DecoderFailureTmp = new DecoderFailureTmp(message) {}

    def invalidType(clazz: String, value: String) =
      DecoderFailureTmp(s"$value's type is not $clazz")

    def missingField(field: String, value: String) =
      DecoderFailureTmp(s"$field is missing in $value")
  }


  trait Decoder[A] {
    def decode(buffer: MiddleBuffer): Either[DecoderFailureTmp, A]
  }

  object Decoder {
    //summoner
    def apply[A](implicit dec: Decoder[A]): Decoder[A] = dec

    //constructor
    def instance[A](func: MiddleBuffer => Either[DecoderFailureTmp, A]): Decoder[A] = {
      new Decoder[A] {
        override def decode(buffer: MiddleBuffer): Either[DecoderFailureTmp, A] = {
          func(buffer)
        }
      }
    }


    implicit def genericDecoder[A, R](
      implicit
      gen: LabelledGeneric.Aux[A, R],
      dec: Lazy[Decoder[R]]
    ): Decoder[A] = {
      instance[A] { buffer =>
        for {
          r <- dec.value.decode(buffer).right
        } yield gen.from(r)
      }
    }

    implicit def hListDecoder[K <: Symbol, H, T <: HList](
      implicit
      witness: Witness.Aux[K],
      hDecoder: Lazy[Decoder[H]],
      tDecoder: Decoder[T]
    ): Decoder[FieldType[K, H] :: T] = {
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


    trait CoproductTypeDecoder[A] extends Decoder[A] {
      def decodeCoproduct(buffer: MiddleBuffer, nameOption: Option[String]): Either[DecoderFailureTmp, A]

      override def decode(buffer: MiddleBuffer): Either[DecoderFailureTmp, A] = decodeCoproduct(buffer, None)
    }

    implicit def coproductDecoder[K <: Symbol, H, T <: Coproduct](
      implicit
      witness: Witness.Aux[K],
      hDecoder: Lazy[Decoder[H]],
      tDecoder: CoproductTypeDecoder[T]
    ): CoproductTypeDecoder[FieldType[K, H] :+: T] = {
      new CoproductTypeDecoder[FieldType[K, H] :+: T] {
        override def decodeCoproduct(
          buffer: MiddleBuffer,
          nameOption: Option[String]
        ): Either[DecoderFailureTmp, FieldType[K, H] :+: T] = {
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
      new CoproductTypeDecoder[CNil] {
        override def decodeCoproduct(
          buffer: MiddleBuffer,
          nameOption: Option[String]
        ): Either[DecoderFailureTmp, CNil] = {
          Left(DecoderFailureTmp("it should never get to cNilInstance."))
        }
      }
    }

    //instance[CNil] { buffer => throw new Exception("it should never get to cNilInstance.") }

    private def wrapTry[T](func: => T): Either[DecoderFailureTmp, T] = {
      val t = Try(func)
      t.toEither.left.map(e => DecoderFailureTmp(e.getClass + ":" + e.getMessage))
    }

    implicit val intInstance = instance[Int] { buffer => wrapTry(buffer.getInt()) }
    implicit val floatInstance = instance[Float](buffer => wrapTry(buffer.getFloat()))
    implicit val doubleInstance = instance[Double](buffer => wrapTry(buffer.getDouble()))
    implicit val stringInstance = instance[String](buffer => wrapTry(buffer.getString()))
    implicit val booleanInstance = instance[Boolean](buffer => wrapTry(buffer.getByte()).right.map(_ == 1.toByte))


    private def readToArray[A](
      buffer: MiddleBuffer, len: Int, dec: Decoder[A]
    )(implicit m: ClassTag[A]): Either[DecoderFailureTmp, Array[A]] = {
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

    implicit def seqDecoder[A](implicit dec: Decoder[A], m: ClassTag[A]): Decoder[Seq[A]] = {
      instance[Seq[A]] { buffer =>
        val len = buffer.getInt()
        readToArray(buffer, len, dec).right.map(_.toSeq)
      }
    }


    implicit def listDecoder[A](implicit dec: Decoder[A], m: ClassTag[A]): Decoder[List[A]] = {
      instance[List[A]] { buffer =>
        val len = buffer.getInt()
        readToArray(buffer, len, dec).right.map(_.toList)
      }
    }


    implicit def arrayDecoder[A](implicit dec: Decoder[A], m: ClassTag[A]): Decoder[Array[A]] = {
      instance[Array[A]] { buffer =>
        val len = buffer.getInt()
        readToArray(buffer, len, dec)
      }
    }

    implicit def optionDecoder[A](implicit dec: Decoder[A]): Decoder[Option[A]] = {
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
      kDec: Decoder[K],
      vDec: Decoder[V],
      c1: ClassTag[K],
      c2: ClassTag[V]
    ): Decoder[Map[K, V]] = {
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
