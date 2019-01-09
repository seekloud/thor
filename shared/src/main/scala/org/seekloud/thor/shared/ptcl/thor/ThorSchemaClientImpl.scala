package org.seekloud.thor.shared.ptcl.thor

import org.seekloud.thor.shared.ptcl.component.Adventurer
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
  var canvasUnit:Float,
  preCanvas: List[MiddleCanvas] = Nil,
  preCanvasAdventurer: List[MiddleCanvas] = Nil,
) extends ThorSchemaImpl(config, myId, myName)
with AdventurerClient
with FoodClient
with BackgroundClient
with DrawOtherClient
with FpsRender{

  var killerNew : String = "?"
  var duringTime : String = "0"
  val ifTest: Boolean = false
  val preTime: Long = System.currentTimeMillis()

  def drawGame(mainId: String, offSetTime:Long, canvasUnit: Float, canvasBounds: Point): Unit ={
    if(!waitSyncData){
      adventurerMap.get(mainId) match{
        case Some(adventurer) =>
          //保持自己的adventurer在屏幕中央~
          val start = System.currentTimeMillis()
          val moveDistance = getMoveDistance(adventurer, offSetTime)
          val offset = canvasBounds/2 - (adventurer.getAdventurerState.position + moveDistance)

          drawBackground(offset, canvasUnit, canvasBounds)
          drawFood(offset, canvasUnit, canvasBounds)
          drawAdventurers(offSetTime, offset, canvasUnit, canvasBounds)
          drawEnergyBar(adventurer)

          if(ifTest)
            drawAttacking(offset, adventurer, attackingAdventureMap.getOrElse(adventurer.playerId, 3))

          val end = System.currentTimeMillis()
//          if(end-start > 10)println(s"drawTime too Long: ${end-start}")
        case None => println("None!!!!!!")
      }
    }
    else{
      println("waitSyncData!!!!")
    }
  }

  def updateSize(bounds: Point, unit: Float): Unit ={
    canvasSize = bounds
    canvasUnit = unit
  }

}
