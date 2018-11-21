
package org.seekloud.thor.shared.ptcl.util

/**
  * User: Taoz
  * Date: 7/12/2018
  * Time: 1:34 PM
  */
trait MiddleBuffer {

  def clear(): Unit

  def result(): Any

  def putString(s: String): MiddleBuffer = {
    val bytes = s.getBytes("utf-8")
    putInt(bytes.length)
    bytes.foreach { b => putByte(b) }
    this
  }

  def putByte(b: Byte): MiddleBuffer

  def putInt(i: Int): MiddleBuffer

  def putFloat(f: Float): MiddleBuffer

  def putDouble(d: Double): MiddleBuffer

  def putLong(l: Long): MiddleBuffer

  def putBoolean(b: Boolean): MiddleBuffer

  def putChar(c: Char): MiddleBuffer

  def putShort(s: Short): MiddleBuffer

  /*
    def putIntArray(ls: Array[Int]): MiddleBuffer = putXArray(putInt)(ls)

    def putFloatArray(ls: Array[Float]): MiddleBuffer = putXArray(putFloat)(ls)

    def putStringArray(ls: Array[String]): MiddleBuffer = putXArray(putString)(ls)

    def putXArray[A](putXFunc: A => Unit )(ls: Array[A]): MiddleBuffer = {
      putInt(ls.length)
      var i = 0
      while (i < ls.length) {
        putXFunc(ls(i))
        i += 1
      }
      this
    }
  */


  def getString(): String = {
    val len = getInt()
    val bytes = new Array[Byte](len)
    for (i <- 0 until len) {
      bytes(i) = getByte()
    }
    new String(bytes, "utf-8")
  }

  def getByte(): Byte

  def getInt(): Int

  def getFloat(): Float

  def getDouble(): Double

  def getLong(): Long

  def getShort(): Short

  def getBoolean(): Boolean

  def getChar(): Char


  /*
    def getIntArray(): Array[Int] = getXArray(getInt)

    def getFloatArray(): Array[Float] = getXArray(getFloat)

    def getStringArray(): Array[String] = getXArray(getString)

    private def getXArray[T](getFunc: () => T)(implicit m: ClassTag[T]): Array[T] = {
      val len = getInt()
      val ls = new Array[T](len)
      var c = 0
      while (c < len) {
        ls(c) = getFunc()
        c += 1
      }
      ls
    }
  */

}

