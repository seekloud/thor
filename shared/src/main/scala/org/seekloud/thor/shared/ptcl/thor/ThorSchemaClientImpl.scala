package org.seekloud.thor.shared.ptcl.thor

import org.scalajs.dom
import org.seekloud.thor.shared.ptcl.config.ThorGameConfig
import org.seekloud.thor.shared.ptcl.model.Point
import org.seekloud.thor.shared.ptcl.thor.draw._
import org.seekloud.thor.shared.ptcl.util.middleware._

/**
  * User: TangYaruo
  * Date: 2018/11/12
  * Time: 16:17
  */
case class ThorSchemaClientImpl (
                            drawFrame: MiddleFrame,
                            ctx: MiddleContext,
                             override val config: ThorGameConfig,
                             myId: String,
                             myName: String,
                             var canvasSize:Point,
                             var canvasUnit:Float
                           ) extends ThorSchemaImpl(config, myId, myName)
with AdventurerClient
with FoodClient
with BackgroundClient
with DrawOtherClient
with FpsRender{

  def drawGame(offSetTime:Long, canvasUnit: Float, canvasBounds: Point): Unit ={
    if(!waitSyncData){
      adventurerMap.get(myId) match{
        case Some(adventurer) =>
          //保持自己的adventurer在屏幕中央~
          val moveDistance = getMoveDistance(adventurer, offSetTime)
          val offset = canvasBounds/2 - (adventurer.getAdventurerState.position + moveDistance)

          drawBackground(offset, canvasUnit, canvasBounds)
          drawFood(offset, canvasUnit, canvasBounds)
          drawAdventurers(offSetTime, offset, canvasUnit)
          drawEnergyBar(adventurer)
        case None => ()
      }
    }
  }

  def updateSize(bounds: Point, unit: Float): Unit ={
    canvasSize = bounds
    canvasUnit = unit
  }

}
