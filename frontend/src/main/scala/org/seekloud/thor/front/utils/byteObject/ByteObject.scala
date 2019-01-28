/*
 * Copyright 2018 seekloud (https://github.com/seekloud)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.seekloud.thor.front.utils.byteObject

import org.seekloud.thor.front.utils.byteObject.decoder.{BytesDecoder, DecoderFailure}
import org.seekloud.thor.front.utils.byteObject.encoder.BytesEncoder
import org.seekloud.thor.shared.ptcl.util.MiddleBuffer

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
