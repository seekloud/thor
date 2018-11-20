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
    def drawAnAdventurer(adventurer: Adventurer) = {
      val mapImg = dom.document.createElement("img").asInstanceOf[html.Image]
      mapImg.setAttribute("src", s"/thor/static/img/skins-sheet0-0.png")
      val r = adventurer.getAdventurerState.radius
      val position = adventurer.getAdventurerState.position
      var moveDistance = config.getMoveDistanceByFrame(adventurer.getAdventurerState.speedLevel).rotate(adventurer.getAdventurerState.direction) * offSetTime.toFloat / ptcl.model.Frame.millsAServerFrame
      //如果达到边界 则不再往外走
      if(position.x - r < 0 || position.x + r > config.boundary.x) moveDistance = moveDistance.copy(x = 0)
      if(position.y - r < 0 || position.y + r > config.boundary.y) moveDistance = moveDistance.copy(y = 0)

      val sx = position.x + offset.x + moveDistance.x
      val sy = position.y + offset.y + moveDistance.y
      val dx = 2 * r
      val dy = 2 * r

//      println(s"face:${adventurer.getAdventurerState.faceDirection} direction:${adventurer.getAdventurerState.direction}")
      ctx.translate((sx + r) * canvasUnit, (sy + r) * canvasUnit)
      ctx.rotate(adventurer.getAdventurerState.direction)
      ctx.drawImage(mapImg, -r * canvasUnit, -r * canvasUnit, dx * canvasUnit, dy * canvasUnit)
      // 恢复设置（恢复的步骤要跟你修改的步骤向反）
      ctx.rotate(-adventurer.getAdventurerState.direction)
      ctx.translate(-(sx + r) * canvasUnit, -(sy + r) * canvasUnit)
      // 之后canvas的原点又回到左上角，旋转角度为0
//      println(s"sx:${adventurer.getAdventurerState.position.x} sy:${adventurer.getAdventurerState.position.y}")
//      ctx.drawImage(mapImg, sx * canvasUnit, sy * canvasUnit, dx * canvasUnit, dy * canvasUnit)
      ctx.restore()
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






