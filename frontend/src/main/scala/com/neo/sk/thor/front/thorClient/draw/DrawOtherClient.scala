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
  mapImg.setAttribute("src", s"${Routes.base}/static/img/bigmap.png")

  def drawBackground():Unit = {
    ctx.save()
    val pat = ctx.createPattern(mapImg,"repeat")
    ctx.fillStyle = pat
    ctx.fillRect(0,0,dom.window.innerWidth ,dom.window.innerHeight)
    ctx.fill()
    ctx.restore()
  }

  def drawGameStop(): Unit = {
    ctx.fillStyle = Color.Black.toString()
    ctx.fillRect(0, 0, dom.window.innerWidth.toFloat, dom.window.innerHeight.toFloat)
    ctx.fillStyle = "rgb(250, 250, 250)"
    ctx.textAlign = "left"
    ctx.textBaseline = "top"
    ctx.font = "36px Helvetica"
    ctx.fillText(s"您已经死亡,被玩家=${}所杀", 150, 180)
    println()
  }

}
