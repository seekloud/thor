/*
 * Copyright 2018 seekloud (https://github.com/seekloud)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.seekloud.thor.shared.ptcl.thor.draw

import org.seekloud.thor.shared.ptcl.model.{Point, Score}
import org.seekloud.thor.shared.ptcl.thor.ThorSchemaClientImpl
import org.seekloud.thor.shared.ptcl.model.Constants._

trait BackgroundClient {
  this: ThorSchemaClientImpl =>


  def window = Point((canvasSize.x - 12).toFloat, (canvasSize.y - 12).toFloat)
  def baseFont: Float = window.x / 1440
  private val mapImg = drawFrame.createImage(pictureMap("background.png"))

  def drawBackground(offset: Point, canvasUnit: Float, canvasBoundary: Point): Unit = {
    ctx.save()
    ctx.setFill("#171b1f")
    ctx.fillRec(0, 0, canvasBoundary.x * canvasUnit, canvasBoundary.y * canvasUnit)
    ctx.fill()
    ctx.drawImage(mapImg, offset.x * canvasUnit, offset.y * canvasUnit, Some(config.boundary.x/2 * canvasUnit, config.boundary.y/2 * canvasUnit))
    ctx.drawImage(mapImg, (offset.x + config.boundary.x/2) * canvasUnit, offset.y * canvasUnit, Some(config.boundary.x/2 * canvasUnit, config.boundary.y/2 * canvasUnit))
    ctx.drawImage(mapImg, offset.x * canvasUnit, (offset.y + config.boundary.y/2) * canvasUnit, Some(config.boundary.x/2 * canvasUnit, config.boundary.y/2 * canvasUnit))
    ctx.drawImage(mapImg, (offset.x + config.boundary.x/2) * canvasUnit, (offset.y + config.boundary.y/2) * canvasUnit, Some(config.boundary.x/2 * canvasUnit, config.boundary.y/2 * canvasUnit))
    val borderW = 10
    ctx.setFill("#4A4B49")
    ctx.rect(offset.x * canvasUnit, offset.y * canvasUnit, config.boundary.x * canvasUnit, borderW)
    ctx.rect(offset.x * canvasUnit, offset.y * canvasUnit, borderW, config.boundary.y * canvasUnit)
    ctx.rect((offset.x + config.boundary.x) * canvasUnit - borderW, offset.y * canvasUnit, borderW, config.boundary.y * canvasUnit)
    ctx.rect(offset.x * canvasUnit, (offset.y + config.boundary.y) * canvasUnit - borderW, config.boundary.x * canvasUnit, borderW)

    ctx.fill()
    ctx.restore()
  }

  def drawBarrage(s: String, t: String): Unit = {
    ctx.save()
    ctx.setFont("Comic Sans Ms", 25)
    ctx.setTextBaseLine("top")
    ctx.setFill("#ffffff")
    if (t == "join") {
      println("join")
      val tmp = s + "加入了游戏"
      ctx.fillText(tmp, window.x * 0.38, window.y * 0.17)
    }
    else if (t == "left"){
      println("left")
      val tmp = s + "离开了游戏"
      ctx.fillText(tmp, window.x * 0.38, window.y * 0.17)
    }
    else{
      val hammerImg = drawFrame.createImage(pictureMap("hammer.png"))
      val start = window.x * 0.5 - (ctx.measureText(s"$s $t") + 80)/2
      ctx.fillText(s, start, window.y * 0.17)
      ctx.drawImage(hammerImg, start + ctx.measureText(s) + 25, window.y * 0.15, Some(50, 50))
      ctx.fillText(t, start + ctx.measureText(s) + 100 , window.y * 0.17)
    }

    ctx.restore()
  }


  def drawTextLine(str: String, x: Double, lineNum: Int, lineBegin: Int = 0 , tp:Int): Unit = {
    ctx.save()
    ctx.setTextBaseLine("top")
    if (tp == 1)
      ctx.setFont("Comic Sans MS", baseFont * 20)
    else if (tp == 2){
      ctx.setFont("Comic Sans MS", baseFont * 16)
      ctx.setFill("#fdffff")}
    else{
      ctx.setFont("Comic Sans MS", baseFont * 16)
      ctx.setFill("#ffff00")
    }

    ctx.fillText(str, x, (lineNum + lineBegin) * window.x * 0.01)
    ctx.restore()
  }

  def drawSmallMap(mainId: String): Unit ={

    def drawStar(adventurerMapX: Double, adventurerMapY: Double) = {
      ctx.save()
      ctx.beginPath()
      ctx.moveTo(adventurerMapX - 6.6, adventurerMapY - 2.0)
      ctx.lineTo(adventurerMapX + 6.6, adventurerMapY - 2.0)
      ctx.lineTo(adventurerMapX - 4.0, adventurerMapY + 6.0)
      ctx.lineTo(adventurerMapX - 0.0, adventurerMapY - 7.0)
      ctx.lineTo(adventurerMapX + 4.0, adventurerMapY + 6.0)
      ctx.setFill("#b1cfed")
      ctx.fill()
      ctx.restore()
    }

    def drawCrown(adventurerMapX: Double, adventurerMapY: Double) = {
      val img = drawFrame.createImage(pictureMap("crown.png"))
      ctx.save()
      ctx.beginPath()
      ctx.drawImage(img, adventurerMapX - 6, adventurerMapY - 6, Some(12,12))
      ctx.restore()
    }


    //获取比例
    val scale = (window.x * 0.2) / 600.0
    ctx.save()
    ctx.setFill("rgba(0,0,0,0.4)")
    ctx.fillRec(10, window.y - window.x * 0.11, window.x * 0.2, window.x * 0.1)
    adventurerMap.foreach{
      case adventurer if adventurer._1 == mainId =>
        val adventurerMapX = 10 + adventurer._2.position.x * scale
        val adventurerMapY = window.y - window.x * 0.11 + adventurer._2.position.y * scale
        drawStar(adventurerMapX, adventurerMapY)
      case adventurer if adventurer._2.level >= 21 =>
        val adventurerMapX = 10 + adventurer._2.position.x * scale
        val adventurerMapY =  window.y - window.x * 0.11 + adventurer._2.position.y * scale
        drawCrown(adventurerMapX, adventurerMapY)
      case _ => ()
    }
    ctx.restore()
  }

  def drawRank(Rank: List[Score], CurrentOrNot: Boolean, shortId: Byte): Unit = {
    val text = "———————排行榜———————"
    val RankBaseLine = 3
    var index = 0
    var yourRank = 100
    var yourNameIn = false
    val begin = if (CurrentOrNot) 10 else window.x * 0.78
    var last = ""
    ctx.save()
    ctx.setFill("rgba(0,0,0,0.6)")
    ctx.fillRec(begin, 0, window.x * 0.25, window.x * 0.25)
    ctx.setFill("#fdffff")
    ctx.setTextAlign("start")
    drawTextLine(s"   $text", 10 + window.x * 0.01, 0, 2,1)

    Rank.foreach { score =>
      index += 1
      if (score.bId == shortId) yourRank = index
      if (playerIdMap.exists(_._1 == score.bId)) {
        val fullName = playerIdMap(score.bId)._2
        val name = if (fullName.length <= 4) fullName.take(4) else fullName.take(4) + "..."
        if (index < 10) {
          ctx.setTextAlign("left")
          if (score.bId == shortId) {
            yourNameIn = true
            drawTextLine(s"【$index】  $name ", begin + window.x * 0.01, index * 2, RankBaseLine,3)
            drawTextLine(s" 分数: ${score.e}", begin + window.x * 0.11, index * 2, RankBaseLine,3)
            drawTextLine(s" 击杀数: ${score.k}", begin + window.x * 0.18, index * 2, RankBaseLine,3)
          }
          else{
            drawTextLine(s"【$index】  $name ", begin + window.x * 0.01, index * 2, RankBaseLine,2)
            drawTextLine(s" 分数: ${score.e}", begin + window.x * 0.11, index * 2, RankBaseLine,2)
            drawTextLine(s" 击杀数: ${score.k}", begin + window.x * 0.18, index * 2, RankBaseLine,2)
          }

        }
      }
    }
    index += 1
    if (!yourNameIn) {
      Rank.find(_.bId == shortId) match {
        case Some(yourScore) =>
          if (playerIdMap.exists(_._1 == yourScore.bId)) {
            val fullName = playerIdMap(yourScore.bId)._2
            val name = if (fullName.length <= 4) fullName.take(4) + "   " else fullName.take(4) + "..."
            drawTextLine(s"【$yourRank】  $name ", begin + window.x * 0.01, 20, RankBaseLine,3)
            drawTextLine(s" 分数: ${yourScore.e}", begin + window.x * 0.11, 20, RankBaseLine,3)
            drawTextLine(s" 击杀数: ${yourScore.k}", begin + window.x * 0.18, 20, RankBaseLine,3)
          }
        case None =>
      }
    }
    ctx.restore()
  }


  def drawGameStop(killerName: String, killNum: Int, energy: Int, level: Int): Unit ={
    ctx.save()
    ctx.beginPath()
    ctx.setFill("rgba(250,250,250,0.6)")
    ctx.fillRec(0,0,canvasSize.x,canvasSize.y)
    ctx.setFill("rgb(250, 250, 250)")
    ctx.setTextAlign("left")
    ctx.setFont("Helvetica", baseFont * 26)
    ctx.fillText("kills", window.x * 0.38, window.y * 0.32, window.x * 0.15)
    ctx.fillText("score", window.x * 0.475, window.y * 0.32, window.x * 0.15)
    ctx.fillText("time", window.x * 0.58, window.y * 0.32, window.x * 0.15)
    ctx.setFont("Helvetica", baseFont * 22)
    ctx.fillText(killNum.toString, window.x * 0.39, window.y * 0.4, window.x * 0.15)
    ctx.fillText(energy.toString, window.x * 0.49, window.y * 0.4, window.x * 0.15)
    ctx.fillText(this.duringTime, window.x * 0.57, window.y * 0.4, window.x * 0.15)
    ctx.save()
    ctx.setStrokeStyle("#ffff00")
    ctx.moveTo(window.x * 0.36, window.y * 0.45)
    ctx.lineTo(window.x * 0.64, window.y * 0.45)
    ctx.stroke()
    ctx.restore()
    ctx.setTextBaseLine("top")
    ctx.setFont("Comic Sans Ms", baseFont * 26)
    ctx.setFill("#ffa400")
    ctx.fillText(s"You Dead,Killer is ${killerName.take(5)} ", window.x * 0.4, window.y * 0.48)
    ctx.fillText(s"Your Final level is $level / 21", window.x * 0.4, window.y * 0.55)
    ctx.setFont("Comic Sans Ms", baseFont * 24)
    ctx.setFill("#FFFFFF")
    ctx.fillText("Click to restart", window.x * 0.42, window.y * 0.72, window.x * 0.15)
    ctx.restore()
  }

  def drawGameStop1(killerName: String, killNum: Int, energy: Int, level: Int): Unit = {
    ctx.save()
    ctx.beginPath()
    ctx.setFill("#000000")
    ctx.fillRec(0, 0, canvasSize.x, canvasSize.y)
//    ctx.drawImage(logo, window.x * 0.375, 20, Some(window.x * 0.25, window.y * 0.25))
//    ctx.drawImage(deadBlank, window.x * 0.35, window.y * 0.3, Some(window.x * 0.3, window.y / 3))
    ctx.setFill("rgb(250, 250, 250)")
    ctx.setTextAlign("left") 
    ctx.setFont("Helvetica", baseFont * 26)
    ctx.fillText("kills", window.x * 0.38, window.y * 0.32, window.x * 0.15)
    ctx.fillText("score", window.x * 0.475, window.y * 0.32, window.x * 0.15)
    ctx.fillText("time", window.x * 0.58, window.y * 0.32, window.x * 0.15)
    ctx.setFont("Helvetica", baseFont * 22)
    ctx.fillText(killNum.toString, window.x * 0.39, window.y * 0.4, window.x * 0.15)
    ctx.fillText(energy.toString, window.x * 0.49, window.y * 0.4, window.x * 0.15)
    ctx.fillText(this.duringTime, window.x * 0.57, window.y * 0.4, window.x * 0.15)
    ctx.save()
    ctx.setStrokeStyle("#ffff00")
    ctx.moveTo(window.x * 0.36, window.y * 0.45)
    ctx.lineTo(window.x * 0.64, window.y * 0.45)
    ctx.stroke()
    ctx.restore()
    ctx.setTextBaseLine("top")
    ctx.setFont("Comic Sans Ms", baseFont * 26)
    ctx.setFill("#ffa400")
    ctx.fillText(s"You Dead,Killer is ${killerName.take(5)} ", window.x * 0.4, window.y * 0.48)
    ctx.fillText(s"Your Final level is $level / 20", window.x * 0.4, window.y * 0.55)
    ctx.setFont("Comic Sans Ms", baseFont * 24)
    ctx.setFill("#000000")
    ctx.fillText("Press space to restart", window.x * 0.42, window.y * 0.72, window.x * 0.15)
    ctx.restore()
  }

  def drawReplayMsg(m: String): Unit = {
    ctx.setFill("#000000")
    ctx.fillRec(0, 0, canvasSize.x, canvasSize.y)
    ctx.setFill("rgb(250, 250, 250)")
    ctx.setTextAlign("left")
    ctx.setTextBaseLine("top")
    ctx.setFont("Helvetica", 3.6 * canvasUnit)
    ctx.fillText(m, 150, 180)
    println()
  }

  def drawGameLoading(): Unit = {
//    println("linking...")
    ctx.setFill("#000000")
    ctx.fillRec(0, 0, canvasSize.x, canvasSize.y)
    ctx.setFill("rgb(250, 250, 250)")
    ctx.setTextAlign("left")
    ctx.setFont("Helvetica", 36)
    ctx.fillText("请稍等，正在连接服务器", 150, 180)
  }

}
