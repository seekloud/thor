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

    def in(view:Point,extra:Point) = {
      x >= (0 - extra.x) && y >= (0 - extra.y) && x <= (view.x + extra.x) && y <= (view.y + extra.y)
    }
  }
  trait Shape{
    protected var position: Point

    def isIntersects(o:Shape):Boolean
  }

  case class Rectangle(topLeft: Point, downRight: Point) extends Shape {

    override protected var position: Point = (topLeft + downRight) / 2
    private val width:Float = math.abs(downRight.x - topLeft.x)
    private val height:Float = math.abs(downRight.y - topLeft.y)


    override def isIntersects(o: Shape): Boolean = {
      o match {
        case t:Rectangle =>
          intersects(t)

        case _ =>
          false
      }
    }

    def intersects(r: Rectangle):Boolean = {
      val (rx,rw,ry,rh) = (r.topLeft.x,r.downRight.x,r.topLeft.y,r.downRight.y)
      val (tx,tw,ty,th) = (topLeft.x,downRight.x,topLeft.y,downRight.y)


      (rw < rx || rw > tx) &&
      (rh < ry || rh > ty) &&
      (tw < tx || tw > rx) &&
      (th < ty || th > ry)
    }

    def intersects(r:Circle):Boolean ={
      if(r.center > topLeft && r.center < downRight){
        true
      }else{
        val relativeCircleCenter:Point = r.center - position
        val dx = math.min(relativeCircleCenter.x, width / 2)
        val dx1 = math.max(dx, - width / 2)
        val dy = math.min(relativeCircleCenter.y, height / 2)
        val dy1 = math.max(dy, - height / 2)
        Point(dx1,dy1).distance(relativeCircleCenter) < r.r
      }
    }
  }

  case class Circle(center:Point,r:Float) extends Shape{

    override protected var position: Point = center


    override def isIntersects(o: Shape): Boolean = {
      o match {
        case t:Rectangle => intersects(t)
        case t:Circle => intersects(t)
      }
    }

    def intersects(r: Rectangle):Boolean = {
      r.intersects(this)
    }

    def intersects(r: Circle):Boolean = {
      r.center.distance(this.center) <= (r.r + this.r)
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
