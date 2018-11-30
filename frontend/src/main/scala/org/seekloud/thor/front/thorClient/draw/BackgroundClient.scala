package org.seekloud.thor.front.thorClient.draw

import org.scalajs.dom
import org.scalajs.dom.ext.Color
import org.scalajs.dom.html
import org.seekloud.thor.front.common.Routes
import org.seekloud.thor.front.thorClient.ThorSchemaClientImpl
import org.seekloud.thor.shared.ptcl.model.{Point, Score}

import scala.collection.mutable

trait BackgroundClient { this: ThorSchemaClientImpl =>


  private val cacheCanvasMap = mutable.HashMap.empty[String, html.Canvas]
  val window = Point((dom.window.innerWidth - 12).toFloat,(dom.window.innerHeight - 12).toFloat)
  val baseFont = window.x / 1440
  private val mapImg = dom.document.createElement("img").asInstanceOf[html.Image]
  mapImg.setAttribute("src", s"${Routes.base}/static/img/map.jpg")
  private val rankImg = dom.document.createElement("img").asInstanceOf[html.Image]
  rankImg.setAttribute("src",s"${Routes.base}/static/img/rank.png")

  def drawBackground(offset: Point, canvasUnit: Int, canvasBoundary: Point):Unit = {
    ctx.save()
    ctx.fillStyle = "#171b1f"
    ctx.fillRect(0, 0, canvasBoundary.x * canvasUnit, canvasBoundary.y * canvasUnit)
    ctx.fill()
    ctx.drawImage(mapImg, offset.x * canvasUnit, offset.y * canvasUnit, config.boundary.x * canvasUnit, config.boundary.y * canvasUnit)
    ctx.restore()
  }

  def drawBarrage(s:String,x:Double,y:Double):Unit={
    ctx.save()
    ctx.font="30px Comic Sans Ms"
    ctx.fillStyle=Color.White.toString()
    ctx.fillText(s,x,y)
    ctx.restore()
  }




  def drawTextLine(str: String, x: Double, lineNum: Int, lineBegin: Int = 0) = {
    ctx.save()
    ctx.textBaseline = "top"
    ctx.font = s"${baseFont * 16}px Comic Sans MS"
    ctx.fillText(str, x, (lineNum + lineBegin - 1) * 14)
    ctx.restore()
  }

  def drawRank(Rank:List[Score],CurrentOrNot:Boolean,id:String):Unit={
    val RankBaseLine = 2
    var index = 0
    var yourRank = 100
    var yourNameIn = false
    val begin = if (CurrentOrNot) 10 else window.x * 0.82

    ctx.save()
    ctx.drawImage(rankImg, begin,0,window.x * 0.18,window.x * 0.18)
    ctx.fillStyle="#ffffff"
    ctx.globalAlpha = 0.6
    Rank.foreach { score =>
      index += 1
      if (score.id == id) yourRank = index
      if (index<6){
        if (score.id == id) yourNameIn = true
        drawTextLine(s" $index:  ${score.n.take(5)}    score=${score.e}   kill=${score.k}", begin + window.x * 0.08, index*2, RankBaseLine)
      }
    }
    index+=1
    if (!yourNameIn){
      ctx.fillStyle="#FFFF00"
      Rank.find(_.id == id) match {
        case Some(yourScore) =>
          drawTextLine(s" $yourRank :  ${yourScore.n.take(5)}    score=${yourScore.e}   kill=${yourScore.k}", begin+10, 12, RankBaseLine)
        case None =>
      }
    }
    ctx.restore()
  }


  private val logo = dom.document.createElement("img").asInstanceOf[html.Image]
  logo.setAttribute("src",s"${Routes.base}/static/img/logo.png")
  private val deadBlank = dom.document.createElement("img").asInstanceOf[html.Image]
  deadBlank.setAttribute("src",s"${Routes.base}/static/img/dead-blank.png")
  private val userName = dom.document.createElement("img").asInstanceOf[html.Image]
  userName.setAttribute("src",s"${Routes.base}/static/img/user-name.png")
  private val playAgain = dom.document.createElement("img").asInstanceOf[html.Image]
  playAgain.setAttribute("src",s"${Routes.base}/static/img/play-again.png")


  def drawGameStop(name:String,kill:Int,score:Int,level:Int,killer: String,time: String): Unit = {
    ctx.save()
    ctx.fillStyle = Color.Black.toString()
    ctx.fillRect(0, 0, dom.window.innerWidth.toFloat, dom.window.innerHeight.toFloat)
    ctx.drawImage(logo,window.x * 0.375,20,window.x * 0.25,window.y * 0.25)
    ctx.drawImage(deadBlank,window.x * 0.35,window.y * 0.3,window.x * 0.3,window.y / 3)
    ctx.fillStyle = "rgb(250, 250, 250)"
    ctx.textAlign = "left"
    ctx.textBaseline = "top"
    ctx.font = s"${baseFont * 26}px Helvetica"
    ctx.fillText("kills",window.x * 0.38, window.y * 0.32, window.x * 0.15)
    ctx.fillText("score",window.x * 0.475, window.y * 0.32, window.x * 0.15)
    ctx.fillText("time",window.x * 0.58, window.y * 0.32, window.x * 0.15)
    ctx.font = s"${baseFont * 22}px Helvetica"
    ctx.fillText(kill.toString,window.x * 0.39, window.y * 0.4, window.x * 0.15)
    ctx.fillText(score.toString,window.x * 0.49, window.y * 0.4, window.x * 0.15)
    ctx.fillText(time,window.x * 0.57, window.y * 0.4, window.x * 0.15)
    ctx.save()
    ctx.strokeStyle = Color.Yellow.toString()
    ctx.moveTo(window.x * 0.36,window.y * 0.45)
    ctx.lineTo(window.x * 0.64,window.y * 0.45)
    ctx.stroke()
    ctx.restore()
    ctx.font = s"${baseFont * 26}px Comic Sans Ms"
    ctx.fillStyle = "orange"
    ctx.fillText(s"You Dead,Killer is ${killer} ", window.x * 0.4, window.y * 0.48)
    ctx.fillText(s"Your Final level is ${level} / 9",window.x * 0.4, window.y * 0.55)


    ctx.drawImage(userName,window.x * 0.4, window.y * 0.7, window.x * 0.2, window.y * 0.1)
    ctx.font = s"${baseFont * 24}px Comic Sans Ms"
    ctx.fillStyle = Color.Black.toString()
    ctx.fillText("Press space to restart",window.x * 0.42, window.y * 0.72, window.x * 0.15)
    ctx.drawImage(playAgain,window.x * 0.4,window.y * 0.85,window.x * 0.2,window.y * 0.1)
    ctx.restore()
  }

}
