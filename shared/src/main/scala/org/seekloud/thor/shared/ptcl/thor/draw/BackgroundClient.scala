package org.seekloud.thor.shared.ptcl.thor.draw

import org.seekloud.thor.shared.ptcl.model.{Point, Score}
import org.seekloud.thor.shared.ptcl.thor.ThorSchemaClientImpl
import org.seekloud.thor.shared.ptcl.model.Constants._

trait BackgroundClient {
  this: ThorSchemaClientImpl =>


  def window = Point((canvasSize.x - 12).toFloat, (canvasSize.y - 12).toFloat)
  def baseFont = window.x / 1440
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
    ctx.restore()
  }

  def drawBarrage(s: String): Unit = {
    ctx.save()
    ctx.setFont("Comic Sans Ms", 30)
    ctx.setTextBaseLine("top")
    ctx.setFill("#ffffff")
    ctx.fillText(s, window.x * 0.38, window.y * 0.17)
    ctx.restore()
  }


  def drawTextLine(str: String, x: Double, lineNum: Int, lineBegin: Int = 0 , tp:Int) = {
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

  def drawRank(Rank: List[Score], CurrentOrNot: Boolean, id: String): Unit = {
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
      if (score.id == id) yourRank = index
      val name = if (score.n.length <= 4) score.n.take(4) else score.n.take(4) + "..."
      if (index < 10) {
        ctx.setTextAlign("left")
        if (score.id == id) {
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
    index += 1
    if (!yourNameIn) {
      ctx.setFill("#FFFF00")
      Rank.find(_.id == id) match {
        case Some(yourScore) =>
          val name = if (yourScore.n.length <= 4) yourScore.n.take(4) + "   " else yourScore.n.take(4) + "..."
          drawTextLine(s"【$yourRank】  $name ", begin + window.x * 0.01, 20, RankBaseLine,3)
          drawTextLine(s" 分数: ${yourScore.e}", begin + window.x * 0.11, 20, RankBaseLine,3)
          drawTextLine(s" 击杀数: ${yourScore.k}", begin + window.x * 0.18, 20, RankBaseLine,3)
        case None =>
      }
    }
    ctx.restore()
  }


  def drawGameStop(killerName: String, killNum: Int, energy: Int, level: Int): Unit ={
    ctx.save()
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
    println("loading")
    ctx.setFill("#000000")
    ctx.fillRec(0, 0, canvasSize.x, canvasSize.y)
    ctx.setFill("rgb(250, 250, 250)")
    ctx.setTextAlign("left")
    ctx.setFont("Helvetica", 36)
    ctx.fillText("请稍等，正在连接服务器", 150, 180)
  }

}
