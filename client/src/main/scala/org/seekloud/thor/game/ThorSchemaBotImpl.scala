package org.seekloud.thor.game

import javafx.scene.image.Image
import org.seekloud.thor.shared.ptcl.config.ThorGameConfig
import org.seekloud.thor.shared.ptcl.model.Point
import org.seekloud.thor.shared.ptcl.thor.ThorSchemaImpl
import org.seekloud.thor.shared.ptcl.util.middleware.MiddleFrame
import org.seekloud.thor.utils.middleware.MiddleContextInFx

/**
  * User: TangYaruo
  * Date: 2018/11/12
  * Time: 16:17
  */
case class ThorSchemaBotImpl (
  drawFrame: MiddleFrame,
  ctx: Map[String,MiddleContextInFx],
  override val config: ThorGameConfig,
  myId: String,
  myName: String,
  var canvasSize:Point,
  var canvasUnit:Float,
) extends ThorSchemaImpl(config, myId, myName)
 {

  var preFoodImage:List[Image] = List.empty
  var preAdventurerImage:List[Image] = List.empty
  var preWeaponImage:List[Image] = List.empty
  var preDeathImage:List[Image] = List.empty
  var killerNew : String = "?"
  var duringTime : String = "0"
  val ifTest: Boolean = false
  val preTime: Long = System.currentTimeMillis()


//  def drawGame4Client(mainId: String, offSetTime:Long, canvasUnit: Float, canvasBounds: Point): Unit ={
//    if(!waitSyncData){
//      adventurerMap.get(mainId) match{
//        case Some(adventurer) =>
//          //保持自己的adventurer在屏幕中央~
//          val moveDistance = getMoveDistance(adventurer, offSetTime)
//          val offset = canvasBounds/2 - (adventurer.getAdventurerState.position + moveDistance)
//
//          val a = System.currentTimeMillis()
//          drawBackground(offset, canvasUnit, canvasBounds)
//          drawEnergyBar(adventurer)
//          val b = System.currentTimeMillis()
//        //          if (b-a>5)
//        //            println(s"the span all is ${b-a}")
//
//        case None => println("None!!!!!!")
//      }
//    }
//    else{
//      println("waitSyncData!!!!")
//    }
//  }

  def updateSize(bounds: Point, unit: Float): Unit ={
    canvasSize = bounds
    canvasUnit = unit
  }

  def changePreImage(preFood: List[Image] = Nil,
    preAdventurer: List[Image] = Nil,
    preWeapon: List[Image] = Nil,
    preDeath: List[Image] = Nil): Unit = {
    preFoodImage = preFood
    preAdventurerImage = preAdventurer
    preWeaponImage = preWeapon
    preDeathImage = preDeath
  }
}
