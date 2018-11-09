package com.neo.sk.thor.front.thor

import java.util.concurrent.atomic.AtomicInteger

import com.neo.sk.thor.shared.ptcl
import com.neo.sk.thor.shared.ptcl.model
import com.neo.sk.thor.shared.ptcl.model.{CanvasBoundary, Point}
import com.neo.sk.thor.shared.ptcl.protocol.{WsFrontProtocol, WsProtocol}
import com.neo.sk.thor.shared.ptcl.thor._
import org.scalajs.dom
import org.scalajs.dom.ext.Color
import mhtml._

import scala.xml.Elem
import org.scalajs.dom.html.Image
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLElement

import scala.collection.mutable
import scala.collection.mutable.ListBuffer


class GridClient(override val boundary: model.Point,canvasUnit:Int,canvasBoundary:model.Point) extends Grid {


  override def debug(msg: String): Unit = {}

  override def info(msg: String): Unit = println(msg)


  def playerJoin(adventurer: Adventurer) = {

  }

  def gridSyncState(d:GridState) = {

    systemFrame = d.f

  }


  def draw(ctx:dom.CanvasRenderingContext2D,myName:String,myId:Long,curFrame:Int,canvasBoundary:Point) = {

  }

  def drawByOffsetTime(ctx:dom.CanvasRenderingContext2D,myName:String,myId:Long,offsetTime:Long,canvasBoundary:Point) = {

  }


  private def drawBackground(ctx:dom.CanvasRenderingContext2D,offset:Point,canvasBoundary:Point) = {

  }



  override def update(): Unit = {

    super.update()
  }

  def rollback2State(d:GridState) = {

  }



}
