package org.seekloud.thor.front.thorClient

import org.seekloud.thor.front.thorClient.draw.{AdventurerClient, DrawOtherClient, FoodClient}
import org.seekloud.thor.shared.ptcl
import org.seekloud.thor.shared.ptcl.`object`.Adventurer
import org.seekloud.thor.shared.ptcl.config.{ThorGameConfig, ThorGameConfigImpl}
import org.seekloud.thor.shared.ptcl.model.Point
import org.seekloud.thor.shared.ptcl.thor.ThorSchemaImpl
import org.scalajs.dom

import scala.collection.mutable

/**
  * User: TangYaruo
  * Date: 2018/11/12
  * Time: 16:17
  */
case class ThorSchemaClientImpl (
                             protected val ctx:dom.CanvasRenderingContext2D,
                             override val config: ThorGameConfig,
                             myId: String,
                             myName: String
                           ) extends ThorSchemaImpl(config, myId, myName)
with AdventurerClient
with FoodClient
with DrawOtherClient{

//  protected val adventurerAttackAnimationMap: mutable.HashMap[Int, Int] = mutable.HashMap[Int, Int]() //可能存在的挥刀动画 id->动画id

  def drawGame(offSetTime:Long, canvasUnit: Int, canvasBounds: Point): Unit ={
    if(!waitSyncData){
      adventurerMap.get(myId) match{
        case Some(adventurer) =>
          //TODO 各种环境绘画
          //保持自己的adventurer在屏幕中央~
          val r = adventurer.getAdventurerState.radius
          val position = adventurer.getAdventurerState.position
          var moveDistance = config.getMoveDistanceByFrame(adventurer.getAdventurerState.speedLevel).rotate(adventurer.getAdventurerState.direction) * offSetTime.toFloat / config.frameDuration
          //如果达到边界 则不再往外走
          val delay = 0.5
          if(position.x - r < delay || position.x + r > config.boundary.x - delay) moveDistance = moveDistance.copy(x = 0)
          if(position.y - r < delay || position.y + r > config.boundary.y - delay) moveDistance = moveDistance.copy(y = 0)
//          println(s"position $position r $r")
          val offset = canvasBounds/2 - (adventurer.getAdventurerState.position - Point(r, r) + moveDistance)

          drawBackground(offset, canvasUnit, canvasBounds)
          drawFoodByOffsetTime(offset, canvasUnit, canvasBounds)
          drawAdventurerByOffsetTime(offSetTime, offset, canvasUnit)
          drawEnergyBar(adventurer)
        case None =>()
      }
    }
  }
}
