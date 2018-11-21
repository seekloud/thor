package org.seekloud.utils.byteObject


import org.seekloud.thor.shared.ptcl.util.MiddleBuffer
import org.seekloud.utils.byteObject.decoder.{BytesDecoder, DecoderFailure}
import org.seekloud.utils.byteObject.encoder.BytesEncoder

/**
  * User: Taoz
  * Date: 7/16/2018
  * Time: 10:47 AM
  */
object ByteObject {


  implicit final class EncoderOps[A](val wrappedEncodeable: A) extends AnyVal {
    final def fillMiddleBuffer[B <: MiddleBuffer](
      buffer: B
    )(implicit encoder: BytesEncoder[A]): B = {
      buffer.clear()
      encoder.encode(wrappedEncodeable, buffer)
      buffer
    }
  }

  def bytesDecode[A](input: MiddleBuffer)(implicit decoder: BytesDecoder[A]): Either[DecoderFailure, A] = {
    decoder.decode(input)
  }


}
