package com.neo.sk.thor.front.thorClient.draw

import com.neo.sk.thor.front.common.Routes
import com.neo.sk.thor.front.thorClient.ThorSchemaClientImpl
import com.neo.sk.thor.shared.ptcl
import com.neo.sk.thor.shared.ptcl.`object`.Adventurer
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLElement
import org.scalajs.dom.html
import com.neo.sk.thor.shared.ptcl.model.Point

import scala.collection.mutable


/**
  * Created by Jingyi on 2018/11/9

  */

trait AdventurerClient { this: ThorSchemaClientImpl =>

//  private  val mapImg = dom.document.createElement("img").asInstanceOf[html.Image]
//  mapImg.setAttribute("src", s"${Routes.base}/static/img/logo-sheet0.png")

  def drawAdventurer(offSetTime: Long, offset: Point, canvasUnit: Int): Unit ={
    //fixme 垃圾代码
    def drawAnAdventurer(adventurer: Adventurer) = {
//      val mapImg = dom.document.createElement("img").asInstanceOf[html.Image]
//      mapImg.setAttribute("src", s"/thor/static/img/Adventurer-${adventurer.level}.png")
      val r = config.getAdventurerRadiusByLevel(adventurer.level)
      val position = adventurer.getAdventurerState.position
      var moveDistance = config.getMoveDistanceByFrame(adventurer.getAdventurerState.speedLevel).rotate(adventurer.getAdventurerState.direction) * offSetTime.toFloat / config.frameDuration
      //如果达到边界 则不再往外走
      val delay = 0.5
      if(position.x - r < delay || position.x + r > config.boundary.x - delay) moveDistance = moveDistance.copy(x = 0)
      if(position.y - r < delay || position.y + r > config.boundary.y - delay) moveDistance = moveDistance.copy(y = 0)

      val sx = position.x + offset.x + moveDistance.x - r
      val sy = position.y + offset.y + moveDistance.y - r
      val dx = 2 * r
      val dy = 2 * r
      println(s"dx:$dx")

      val src = s"/thor/static/img/Adventurer-${adventurer.level}.png"
      val weapon = s"/thor/static/img/weapon-${adventurer.level}.png"
      CanvasUtils.rotateImage(ctx, src, Point(sx, sy) * canvasUnit, Point(0, 0), dx * canvasUnit, dy * canvasUnit, adventurer.getAdventurerState.direction)

      var step = 3
      var isAttacting = false
      attackingAdventureMap.get(adventurer.playerId) match{
        case Some(s) =>
          step = s
          if(s!=0)
            isAttacting = true
        case _ =>
      }
      val angle = adventurer.getAdventurerState.direction - (step * math.Pi / 3 - math.Pi / 12).toFloat
      val weaponLength = config.getWeaponLengthByLevel(adventurer.getAdventurerState.level)
      val weaponWidth = 5
      val gap = 0
      val move: Float = if(isAttacting) math.Pi.toFloat * 1 / 3 * offSetTime.toFloat / config.frameDuration else 0
//      println(s"d: ${adventurer.getAdventurerState.direction} angle:${angle}")
//      val angle = adventurer.getAdventurerState.direction + 30/180 * math.Pi
//      CanvasUtils.rotateImage(ctx, weapon, Point(sx, sy) * canvasUnit, Point(0, -(r + gap)) * canvasUnit, weaponLength * canvasUnit, weaponWidth * canvasUnit, angle)
      CanvasUtils.rotateImage(ctx, weapon, (Point(sx, sy) + Point(-r, r + gap + weaponLength/2).rotate(angle + move - math.Pi.toFloat/2)) * canvasUnit, Point(0, 0), weaponLength * canvasUnit, weaponWidth * canvasUnit, angle + move)

      val namePosition = position
      ctx.fillStyle = "#ffffff"
      ctx.textAlign = "center"
      ctx.font = "normal normal 20px 楷体"
//      ctx.lineWidth = 2
//      println(s"sx:$sx sy:$sy")
      ctx.textAlign = "center"
      ctx.fillText(s"${adventurer.name}", (sx + r) * canvasUnit, (sy + dy) * canvasUnit + 20)
      ctx.closePath()
    }
    adventurerMap.map{
      adventurer =>
        drawAnAdventurer(adventurer._2)
    }
  }

  def drawAdventurerByOffsetTime(offSetTime: Long, offset: Point, canvasUnit: Int): Unit ={

    drawAdventurer(offSetTime, offset, canvasUnit)
  }

}






