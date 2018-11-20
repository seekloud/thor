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

  private val bar = dom.document.createElement("img").asInstanceOf[html.Image]
  bar.setAttribute("src", s"${Routes.base}/static/img/weaponlevelbar_bg-sheet0.png")
  private val fillBar = dom.document.createElement("img").asInstanceOf[html.Image]
  fillBar.setAttribute("src", s"${Routes.base}/static/img/weaponlevelbar_fg-sheet0.png")

  private val barLength = 288
  private val barHeight = 45
  private val barLeft = (dom.window.innerWidth - barLength)/2
  private val barTop = dom.window.innerHeight - barHeight - 20
  private val maxFillLenght = 696
  def drawEnergyBar(adventurer: Adventurer): Unit = {
    //fixme 垃圾代码
    ctx.save()
    ctx.drawImage(bar, barLeft, barTop, barLength, barHeight )
    ctx.fillStyle = "#ffffff"
    ctx.textAlign = "center"
    ctx.fillText(adventurer.level.toString, barLeft + 18, barTop + 28)
    ctx.restore()
//    println(s"${fillLength}")
    val offsetL = 74
    val offsetT = 15
    val fillMax = barLength-offsetL - 5

    val preLevel = if(adventurer.level == 1) 0 else config.getMaxEnergyByLevel(adventurer.level - 1)
    val nowLevel = config.getMaxEnergyByLevel(adventurer.level)
    val fillLength = (adventurer.energy - preLevel).toFloat / (nowLevel - preLevel) * fillMax
    val rateX = fillMax.toFloat/697
    println(s"energy:${adventurer.energy} pre:$preLevel now:$nowLevel")
    ctx.drawImage(fillBar, (fillMax-fillLength)/rateX,0, fillLength/rateX, 49 , barLeft+offsetL, barTop+offsetT, fillLength, barHeight - offsetT*2 - 1)
  }



}
