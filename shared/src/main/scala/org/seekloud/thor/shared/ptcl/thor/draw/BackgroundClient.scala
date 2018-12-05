package org.seekloud.thor.shared.ptcl.thor.draw


import org.seekloud.thor.shared.ptcl.model.{Point, Score}
import org.seekloud.thor.shared.ptcl.thor.ThorSchemaClientImpl


trait BackgroundClient {
  this: ThorSchemaClientImpl =>


  def window = Point((canvasSize.x - 12).toFloat, (canvasSize.y - 12).toFloat)
  val baseFont = window.x / 1440
  private val mapImg = drawFrame.createImage(s"/img/map.jpg")
  private val rankImg = drawFrame.createImage(s"/img/rank.png")

  def drawBackground(offset: Point, canvasUnit: Float, canvasBoundary: Point): Unit = {
    ctx.save()
    ctx.setFill("#171b1f")
    ctx.fillRec(0, 0, canvasBoundary.x * canvasUnit, canvasBoundary.y * canvasUnit)
    ctx.fill()
    ctx.drawImage(mapImg, offset.x * canvasUnit, offset.y * canvasUnit, Some(config.boundary.x * canvasUnit, config.boundary.y * canvasUnit))
    ctx.restore()
  }

  def drawBarrage(s: String, x: Double, y: Double): Unit = {
    ctx.save()
    ctx.setFont("Comic Sans Ms", 30)
    ctx.setTextBaseLine("top")
    ctx.setFill("#ffffff")
    ctx.fillText(s, x, y)
    ctx.restore()
  }


  def drawTextLine(str: String, x: Double, lineNum: Int, lineBegin: Int = 0) = {
    ctx.save()
    ctx.setTextBaseLine("top")
    ctx.setFont("Comic Sans MS", baseFont * 16)
    ctx.fillText(str, x, (lineNum + lineBegin - 1) * 14)
    ctx.restore()
  }

  def drawRank(Rank: List[Score], CurrentOrNot: Boolean, id: String): Unit = {
    val RankBaseLine = 2
    var index = 0
    var yourRank = 100
    var yourNameIn = false
    val begin = if (CurrentOrNot) 10 else window.x * 0.82

    ctx.save()
    ctx.drawImage(rankImg, begin, 0, Some(window.x * 0.18, window.x * 0.18))
    ctx.setFill("#fdffff")
    Rank.foreach { score =>
      index += 1
      if (score.id == id) yourRank = index
      if (index < 6) {
        if (score.id == id) yourNameIn = true
        ctx.setTextAlign("left")
        drawTextLine(s" $index:  ${score.n.take(5)}    score=${score.e}   kill=${score.k}", begin + window.x * 0.01, index * 2, RankBaseLine)
      }
    }
    index += 1
    if (!yourNameIn) {
      ctx.setFill("#FFFF00")
      Rank.find(_.id == id) match {
        case Some(yourScore) =>
          drawTextLine(s" $yourRank :  ${yourScore.n.take(5)}    score=${yourScore.e}   kill=${yourScore.k}", begin + 10, 12, RankBaseLine)
        case None =>
      }
    }
    ctx.restore()
  }


  private val logo = drawFrame.createImage("/img/logo.png")
  private val deadBlank = drawFrame.createImage("/img/dead-blank.png")
  private val userName = drawFrame.createImage("/img/user-name.png")
  private val playAgain = drawFrame.createImage("/img/play-again.png")


  def drawGameStop(name: String, kill: Int, score: Int, level: Int, killer: String, time: String): Unit = {
    ctx.save()
    ctx.setFill("#000000")
    ctx.fillRec(0, 0, canvasSize.x, canvasSize.y)
    ctx.drawImage(logo, window.x * 0.375, 20, Some(window.x * 0.25, window.y * 0.25))
    ctx.drawImage(deadBlank, window.x * 0.35, window.y * 0.3, Some(window.x * 0.3, window.y / 3))
    ctx.setFill("rgb(250, 250, 250)")
    ctx.setTextAlign("left") 
    ctx.setFont("Helvetica", baseFont * 26)
    ctx.fillText("kills", window.x * 0.38, window.y * 0.32, window.x * 0.15)
    ctx.fillText("score", window.x * 0.475, window.y * 0.32, window.x * 0.15)
    ctx.fillText("time", window.x * 0.58, window.y * 0.32, window.x * 0.15)
    ctx.setFont("Helvetica", baseFont * 22)
    ctx.fillText(kill.toString, window.x * 0.39, window.y * 0.4, window.x * 0.15)
    ctx.fillText(score.toString, window.x * 0.49, window.y * 0.4, window.x * 0.15)
    ctx.fillText(time, window.x * 0.57, window.y * 0.4, window.x * 0.15)
    ctx.save()
    ctx.setStrokeStyle("#ffff00")
    ctx.moveTo(window.x * 0.36, window.y * 0.45)
    ctx.lineTo(window.x * 0.64, window.y * 0.45)
    ctx.stroke()
    ctx.restore()
    ctx.setTextBaseLine("top")
    ctx.setFont("Comic Sans Ms", baseFont * 26)
    ctx.setFill("#ffa400")
    ctx.fillText(s"You Dead,Killer is ${killer.take(5)} ", window.x * 0.4, window.y * 0.48)
    ctx.fillText(s"Your Final level is ${level} / 9", window.x * 0.4, window.y * 0.55)


    ctx.drawImage(userName, window.x * 0.4, window.y * 0.7, Some(window.x * 0.2, window.y * 0.1))
    ctx.setFont("Comic Sans Ms", baseFont * 24)
    ctx.setFill("#000000")
    ctx.fillText("Press space to restart", window.x * 0.42, window.y * 0.72, window.x * 0.15)
    ctx.drawImage(playAgain, window.x * 0.4, window.y * 0.85, Some(window.x * 0.2, window.y * 0.1))
    ctx.restore()
  }

  def drawReplayMsg(m: String): Unit = {
    ctx.setFill("#000000")
    ctx.fillRec(0, 0, canvasSize.x * canvasUnit, canvasSize.y * canvasUnit)
    ctx.setFill("rgb(250, 250, 250)")
    ctx.setTextAlign("left")
    ctx.setTextBaseLine("top")
    ctx.setFont("Helvetica", 3.6 * canvasUnit)
    ctx.fillText(m, 150, 180)
    println()
  }

}