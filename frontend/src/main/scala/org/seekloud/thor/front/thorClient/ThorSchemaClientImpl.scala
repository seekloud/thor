package org.seekloud.thor.front.thorClient

import org.seekloud.thor.front.thorClient.draw.{AdventurerClient, BackgroundClient, DrawOtherClient, FoodClient}
import org.seekloud.thor.shared.ptcl
import org.seekloud.thor.shared.ptcl.component.Adventurer
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
with BackgroundClient
with DrawOtherClient{

  def drawGame(offSetTime:Long, canvasUnit: Int, canvasBounds: Point): Unit ={
    if(!waitSyncData){
      adventurerMap.get(myId) match{
        case Some(adventurer) =>
          //保持自己的adventurer在屏幕中央~
          val moveDistance = getMoveDistance(adventurer, offSetTime)
          val offset = canvasBounds/2 - (adventurer.getAdventurerState.position + moveDistance)

          drawBackground(offset, canvasUnit, canvasBounds)
          drawFood(offset, canvasUnit, canvasBounds)
          drawAdventurerByOffsetTime(offSetTime, offset, canvasUnit)
          drawEnergyBar(adventurer)
        case None =>()
      }
    }
  }
}
