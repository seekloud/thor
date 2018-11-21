package org.seekloud.utils.byteObject

import org.seekloud.thor.shared.ptcl.util.MiddleBuffer
import scala.collection.mutable.ArrayBuffer

/**
  * User: Taoz
  * Date: 7/15/2018
  * Time: 10:50 AM
  */
class MiddleBufferForTest(
  private val internalList: ArrayBuffer[String] = new ArrayBuffer[String]()
) extends MiddleBuffer {


  private var index = 0

  override def clear(): Unit = {
    internalList.clear()
    index = 0
  }

  override def result(): List[String] = internalList.toList

  override def putByte(b: Byte): MiddleBuffer = {
    internalList.append(b.toString)
    this
  }

  override def putInt(i: Int): MiddleBuffer = {
    internalList.append(i.toString)
    this
  }

  override def putFloat(f: Float): MiddleBuffer = {
    internalList.append(f.toString)
    this
  }

  override def getByte(): Byte = {
    val b = internalList(index).toByte
    index += 1
    b
  }

  override def getInt(): Int = {
    val i = internalList(index).toInt
    index += 1
    i
  }


  override def getFloat(): Float = {
    val f = internalList(index).toFloat
    index += 1
    f
  }

  override def getString(): String = {
    val s = internalList(index)
    index += 1
    s
  }

  /*
    def back(): Unit = {
      index -= 1
    }
  */

  override def putString(s: String): MiddleBuffer = {
    internalList.append(s)
    this
  }

  override def putDouble(d: Double): MiddleBuffer = {
    internalList.append(d.toString)
    this
  }

  override def getDouble(): Double = {
    val d = internalList(index).toDouble
    index += 1
    d
  }

  override def putLong(l: Long): MiddleBuffer = {
    internalList.append(l.toString)
    this
  }

  override def putBoolean(b: Boolean): MiddleBuffer = {
    internalList.append(b.toString)
    this
  }

  override def putChar(c: Char): MiddleBuffer = {
    internalList.append(c.toString)
    this
  }

  override def getLong(): Long = {
    val l = internalList(index).toLong
    index += 1
    l
  }

  override def getShort(): Short = {
    val s = internalList(index).toShort
    index += 1
    s
  }

  override def getBoolean(): Boolean = {
    val b = internalList(index).toBoolean
    index += 1
    b
  }


  override def getChar(): Char = {
    val c = internalList(index).toCharArray
    index += 1
    c(0)
  }

  override def putShort(s: Short): MiddleBuffer = {
    internalList.append(s.toString)
    this
  }
}
