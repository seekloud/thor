package com.neo.sk.thor.shared.ptcl

import java.awt.Rectangle
import java.awt.event.KeyEvent
import java.util.concurrent.atomic.AtomicLong


import scala.collection.mutable
import scala.util.Random

package object model {


  val random = new Random(System.currentTimeMillis())

  case class Score(id:Int,n:String,k:Int,d:Int,t:Option[Long] =None)

  case class Point(x: Float, y: Float){
    def +(other: Point) = Point(x + other.x, y + other.y)
    def -(other: Point) = Point(x - other.x, y - other.y)
    def %(other: Point) = Point(x % other.x, y % other.y)
    def <(other: Point) = x < other.x && y < other.y
    def >(other: Point) = x > other.x && y > other.y
    def /(value: Float) = Point(x / value, y / value)
    def *(value: Float) = Point(x * value, y * value)
    def *(other: Point) = x * other.x + y * other.y
    def length = Math.sqrt(lengthSquared)
    def lengthSquared = x * x + y * y
    def distance(other: Point) = Math.sqrt((x - other.x) * (x - other.x) + (y - other.y) * (y - other.y))
    def within(a: Point, b: Point, extra: Point = Point(0, 0)) = {
      import math.{min, max}
      x >= min(a.x, b.x) - extra.x &&
        x < max(a.x, b.x) + extra.y &&
        y >= min(a.y, b.y) - extra.x &&
        y < max(a.y, b.y) + extra.y
    }
    def rotate(theta: Float) = {
      val (cos, sin) = (Math.cos(theta), math.sin(theta))
      Point((cos * x - sin * y).toFloat, (sin * x + cos * y).toFloat)
    }

    def getTheta(center:Point):Double = {
      math.atan2(y - center.y,x - center.x)
    }
  }


  object Boundary{
    val w = 360
    val h = 180

    def getBoundary:Point = Point(w,h)
  }

  object CanvasBoundary{
    val w = 120
    val h = 60

    def getBoundary:Point = Point(w,h)
  }

  object Frame{

    val millsAServerFrame = 120

  }


}
