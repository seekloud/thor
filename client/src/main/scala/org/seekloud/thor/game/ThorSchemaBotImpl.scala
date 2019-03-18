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
case class ThorSchemaBotImpl(
  drawFrame: MiddleFrame,
  ctx: Map[String, MiddleContextInFx],
  override val config: ThorGameConfig,
  myId: String,
  myName: String,
  var canvasSize: Point,
  var canvasUnit: Float,
) extends ThorSchemaImpl(config, myId, myName) {

  var preFoodImage: List[Image] = List.empty
  var preAdventurerImage: List[Image] = List.empty
  var preWeaponImage: List[Image] = List.empty
  var preDeathImage: List[Image] = List.empty
  var killerNew: String = "?"
  var duringTime: String = "0"
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

  def drawGameLoading(): Unit = {
    //    println("linking...")
    ctx("human").setFill("#000000")
    ctx("human").fillRec(0, 0, canvasSize.x, canvasSize.y)
    ctx("human").setFill("rgb(250, 250, 250)")
    ctx("human").setTextAlign("left")
    ctx("human").setFont("Helvetica", 36)
    ctx("human").fillText("请稍等，正在连接服务器", 150, 180)
  }

  def window: Point = Point(canvasSize.x , canvasSize.y) * 4

  def baseFont: Float = window.x / 1440

  def drawGameStop(killerName: String, killNum: Int, energy: Int, level: Int): Unit ={
    ctx("human").save()
    ctx("human").beginPath()
    ctx("human").setFill("rgba(250,250,250,0.6)")
    ctx("human").fillRec(0,0,window.x,window.y)
    ctx("human").setFill("rgb(250, 250, 250)")
    ctx("human").setTextAlign("left")
    ctx("human").setFont("Helvetica", baseFont * 26)
    ctx("human").fillText("kills", window.x * 0.38, window.y * 0.32, window.x * 0.15)
    ctx("human").fillText("score", window.x * 0.475, window.y * 0.32, window.x * 0.15)
    ctx("human").fillText("time", window.x * 0.58, window.y * 0.32, window.x * 0.15)
    ctx("human").setFont("Helvetica", baseFont * 22)
    ctx("human").fillText(killNum.toString, window.x * 0.39, window.y * 0.4, window.x * 0.15)
    ctx("human").fillText(energy.toString, window.x * 0.49, window.y * 0.4, window.x * 0.15)
    ctx("human").fillText(duringTime, window.x * 0.57, window.y * 0.4, window.x * 0.15)
    ctx("human").save()
    ctx("human").setStrokeStyle("#ffff00")
    ctx("human").moveTo(window.x * 0.36, window.y * 0.45)
    ctx("human").lineTo(window.x * 0.64, window.y * 0.45)
    ctx("human").stroke()
    ctx("human").restore()
    ctx("human").setTextBaseLine("top")
    ctx("human").setFont("Comic Sans Ms", baseFont * 26)
    ctx("human").setFill("#ffa400")
    ctx("human").fillText(s"You Dead,Killer is ${killerName.take(5)} ", window.x * 0.4, window.y * 0.48)
    ctx("human").fillText(s"Your Final level is $level / 21", window.x * 0.4, window.y * 0.55)
    ctx("human").setFont("Comic Sans Ms", baseFont * 24)
    ctx("human").setFill("#FFFFFF")
    ctx("human").fillText("Click to restart", window.x * 0.42, window.y * 0.72, window.x * 0.15)
    ctx("human").restore()
  }

  def updateSize(bounds: Point, unit: Float): Unit = {
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
