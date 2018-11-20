package com.neo.sk.thor.front.thorClient.draw
import com.neo.sk.thor.front.common.Routes
import com.neo.sk.thor.front.thorClient.ThorSchemaClientImpl
import com.neo.sk.thor.shared.ptcl.`object`.Adventurer
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLElement
import org.scalajs.dom.html
import com.neo.sk.thor.shared.ptcl.model.Point
import org.scalajs.dom.ext.Color

import scala.collection.mutable
trait DrawOtherClient {this: ThorSchemaClientImpl =>
  private  val mapImg = dom.document.createElement("img").asInstanceOf[html.Image]
  mapImg.setAttribute("src", s"${Routes.base}/static/img/map.jpg")

  def drawBackground(offset: Point, canvasUnit: Int, canvasBoundary: Point):Unit = {
    ctx.save()
//    val pat = ctx.createPattern(mapImg,"repeat")
//    ctx.fillStyle = pat
//    ctx.fillRect(offset.x * canvasUnit,offset.y * canvasUnit,config.boundary.x * canvasUnit ,config.boundary.y * canvasUnit)
//    ctx.fill()
    ctx.fillStyle = "#171b1f"
    ctx.fillRect(0, 0, canvasBoundary.x * canvasUnit, canvasBoundary.y * canvasUnit)
    ctx.fill()
    ctx.drawImage(mapImg, offset.x * canvasUnit, offset.y * canvasUnit, config.boundary.x * canvasUnit, config.boundary.y * canvasUnit)
    ctx.restore()
  }



}
