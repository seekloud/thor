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

package org.seekloud.utils.byteObject

import java.nio.ByteBuffer

import org.seekloud.thor.shared.ptcl.util.MiddleBuffer

/**
  * User: Taoz
  * Date: 7/12/2018
  * Time: 11:23 AM
  */
class MiddleBufferInJvm private() extends MiddleBuffer {

  private[this] var data: ByteBuffer = _

  def this(array: Array[Byte]) {
    this()
    data = ByteBuffer.wrap(array)
  }

  def this(buffer: ByteBuffer) {
    this()
    data = buffer
  }

  def this(size: Int) = {
    this()
    data = ByteBuffer.allocate(size)
  }

  override def clear(): Unit = {
    data.clear()
  }

  override def putByte(b: Byte): MiddleBufferInJvm = {
    data.put(b)
    this
  }

  override def putInt(i: Int): MiddleBufferInJvm = {
    data.putInt(i)
    this
  }

  override def putFloat(f: Float): MiddleBufferInJvm = {
    data.putFloat(f)
    this
  }

  override def putDouble(d: Double): MiddleBufferInJvm = {
    data.putDouble(d)
    this
  }


  override def getByte(): Byte = {
    val rst = data.get()
    //    println(s"getByte: int=[$rst]")
    rst
  }


  override def getInt(): Int = {
    val rst = data.getInt()
    //    println(s"getInt: int=[$rst]")
    rst
  }

  /*
    def getInt(i: Int) : Int = {
      data.getInt(i)
    }
  */

  override def getFloat(): Float = data.getFloat()

  override def getDouble(): Double = data.getDouble

  override def result(): Array[Byte] = {
    //val length = data.position()
    //    println(s"result length: $length")
    data.flip()
    val rst = new Array[Byte](data.limit())
    var c = 0
    while (data.hasRemaining) {
      rst(c) = data.get()
      c += 1
    }
    rst
  }

  override def putLong(l: Long): MiddleBufferInJvm = {
    data.putLong(l)
    this
  }

  override def putBoolean(b: Boolean): MiddleBufferInJvm = {
    val v = if (b) 1 else 0
    putByte(v.toByte)
  }

  override def putChar(c: Char): MiddleBufferInJvm = putInt(c.toInt)

  override def getLong(): Long = data.getLong()

  override def getShort(): Short = data.getShort()

  override def getBoolean(): Boolean = {
    getByte().toInt == 1
  }

  override def getChar(): Char = {
    getInt().toChar
  }

  override def putShort(s: Short): MiddleBufferInJvm = {
    data.putShort(s)
    this
  }
}
