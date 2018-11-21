package org.seekloud.thor.front.utils.byteObject

import org.seekloud.thor.shared.ptcl.util.MiddleBuffer

import scala.scalajs.js

/**
  * User: Taoz
  * Date: 7/12/2018
  * Time: 11:02 AM
  */
class MiddleBufferInJs private() extends MiddleBuffer {


  private[this] var data: js.typedarray.DataView = _
  private[this] var index: Int = -1
  private[this] var capacity = 0

  private val LITTLE_ENDIAN = false

  private def checkCapacity(size: Int): Unit = {
    //    println(s"checkCapacity size=$size index=$index")
    if (index + size > capacity) {
      throw new Exception(s"index[$index] >= capacity[$capacity]")
    }
  }

  override def clear(): Unit = {
    index = 0
  }

  def this(array: js.typedarray.ArrayBuffer) {
    this()
    capacity = array.byteLength
    data = new js.typedarray.DataView(array)
    index = 0
    //    println(s"MiddleBufferInJs capacity=$capacity")
  }

  def this(size: Int) {
    this()
    val in = new js.typedarray.ArrayBuffer(size)
    data = new js.typedarray.DataView(in)
    index = 0
    capacity = size
  }

  override def putByte(b: Byte): MiddleBufferInJs = {
    checkCapacity(1)
    data.setInt8(index, b)
    index += 1
    this
  }

  override def putInt(i: Int): MiddleBufferInJs = {
    checkCapacity(4)
    data.setInt32(index, i, littleEndian = false)
    index += 4
    this
  }

  override def putFloat(f: Float): MiddleBufferInJs = {
    checkCapacity(4)
    data.setFloat32(index, f, littleEndian = false)
    index += 4
    this
  }

  override def putDouble(d: Double): MiddleBufferInJs = {
    checkCapacity(8)
    data.setFloat64(index, d, littleEndian = false)
    index += 8
    this
  }


  override def putString(s: String): MiddleBufferInJs = {
    val bytes = s.getBytes("utf-8")
    checkCapacity(4 + bytes.length)
    putInt(bytes.length)
    bytes.foreach { b => putByte(b) }
    this
  }

  override def getString(): String = {
    val len = getInt()
    //    println(s"getString start, len=$len, index=$index")
    checkCapacity(len)
    val bytes = new Array[Byte](len)
    for (i <- 0 until len) {
      bytes(i) = getByte()
    }
    new String(bytes, "utf-8")
  }

  override def getByte(): Byte = {
    checkCapacity(1)
    //    println(s"getByte, index=$index")
    val b = data.getInt8(index)
    index += 1
    b
  }

  override def getInt(): Int = {
    //    println(s"getInt begin, index=$index")
    checkCapacity(4)
    val i = data.getInt32(index, littleEndian = false)
    index += 4
    //    println(s"getInt end, i=[$i] index=$index")
    i
  }

  override def getFloat(): Float = {
    checkCapacity(4)
    val f = data.getFloat32(index, littleEndian = false)
    index += 4
    f
  }

  override def getDouble(): Double = {
    checkCapacity(8)
    val d = data.getFloat64(index, littleEndian = false)
    index += 8
    d
  }

  override def result(): js.typedarray.ArrayBuffer = {
    data.buffer.slice(0, index)
  }

  override def putLong(l: Long): MiddleBufferInJs = {
    checkCapacity(8)
    val bytes = long2Bytes(l)
    bytes.foreach(b => putByte(b))
    this
  }

  override def putBoolean(b: Boolean): MiddleBufferInJs = {
    val v = if (b) 1 else 0
    putByte(v.toByte)
  }

  override def putChar(c: Char): MiddleBufferInJs = putInt(c.toInt)

  override def putShort(s: Short): MiddleBufferInJs = {
    checkCapacity(2)
    data.setInt16(index, s, littleEndian = false)
    index += 2
    this
  }

  private[this] def bytes2Long(bytes: Array[Byte]): Long = {
    val data = if (LITTLE_ENDIAN) bytes.reverse else bytes
    var value = 0l
    var c = 0
    while (c < 8) {
      val shift = (7 - c) << 3
      value |= (0xff.toLong << shift) & (data(c).toLong << shift)
      c += 1
    }
    value
  }

  private[this] def long2Bytes(l: Long): Array[Byte] = {
    val bytes = new Array[Byte](8)
    var temp = l
    var c = 0
    while (c < 8) {
      bytes(7 - c) = (temp & 0xff).toByte
      temp = temp >> 8
      c += 1
    }
    if (LITTLE_ENDIAN) bytes.reverse else bytes
  }


  override def getLong(): Long = {

    checkCapacity(8)
    val bytes = new Array[Byte](8)
    var c = 0
    while (c < 8) {
      bytes(c) = getByte()
      c += 1
    }
    bytes2Long(bytes)
  }

  override def getShort(): Short = {
    checkCapacity(2)
    val s = data.getInt16(index, littleEndian = false)
    index += 2
    s
  }

  override def getBoolean(): Boolean = {
    getByte().toInt == 1
  }

  override def getChar(): Char = {
    getInt().toChar
  }
}
