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
      val moveDistance = config.getMoveDistanceByFrame(adventurer.getAdventurerState.speedLevel).rotate(adventurer.getAdventurerState.direction) * offSetTime.toFloat / ptcl.model.Frame.millsAServerFrame
      val r = adventurer.getAdventurerState.radius
      val sx = adventurer.getAdventurerState.position.x + offset.x + moveDistance.x
      val sy = adventurer.getAdventurerState.position.y + offset.y + moveDistance.y
      val dx = 2 * r
      val dy = 2 * r
      ctx.drawImage(mapImg, sx * canvasUnit, sy * canvasUnit, dx * canvasUnit, dy * canvasUnit)
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






