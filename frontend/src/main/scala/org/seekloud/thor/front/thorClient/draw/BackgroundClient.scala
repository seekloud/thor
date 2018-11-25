package org.seekloud.thor.front.thorClient.draw

import org.scalajs.dom
import org.scalajs.dom.ext.Color
import org.scalajs.dom.html
import org.seekloud.thor.front.common.Routes
import org.seekloud.thor.front.thorClient.ThorSchemaClientImpl
import org.seekloud.thor.shared.ptcl.model.{Point, Score}

trait BackgroundClient { this: ThorSchemaClientImpl =>

  private val mapImg = dom.document.createElement("img").asInstanceOf[html.Image]
  mapImg.setAttribute("src", s"${Routes.base}/static/img/map.jpg")
  private val rankImg = dom.document.createElement("img").asInstanceOf[html.Image]
  rankImg.setAttribute("src",s"${Routes.base}/static/img/rank.png")

  def drawBackground(offset: Point, canvasUnit: Int, canvasBoundary: Point):Unit = {
    ctx.save()
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

  def drawTextLine(str: String, x: Int, lineNum: Int, lineBegin: Int = 0) = {
    ctx.save()
    ctx.textBaseline = "top"
    ctx.font = "16px Comic Sans MS"
    ctx.fillText(str, x, (lineNum + lineBegin - 1) * 14)
    ctx.restore()
  }

  def drawRank(Rank:List[Score],CurrentOrNot:Boolean,id:String):Unit={
    val RankBaseLine = 2
    var index = 0
    val x=10
    var yourRank = 100
    var yourNameIn = false

    ctx.save()
    ctx.drawImage(rankImg,x,0,250,250)
    ctx.fillStyle="#ffffff"
    ctx.globalAlpha = 0.6
    Rank.foreach { score =>
      index += 1
      if (score.id == id) yourRank = index
      if (index<6){
        if (score.id == id) yourNameIn = true
        drawTextLine(s" $index:  ${score.n.take(5)}    score=${score.e}   kill=${score.k}", x+10, index*2, RankBaseLine)
      }
    }
    index+=1
    if (!yourNameIn){
      ctx.fillStyle="#FFFF00"
      Rank.find(_.id == id) match {
        case Some(yourScore) =>
          drawTextLine(s" $yourRank :  ${yourScore.n.take(5)}    score=${yourScore.e}   kill=${yourScore.k}", x+10, 12, RankBaseLine)
        case None =>
      }
    }
    ctx.restore()
  }

  def drawGameStop(killer: String): Unit = {
    ctx.save()
    ctx.fillStyle = Color.Black.toString()
    ctx.fillRect(0, 0, dom.window.innerWidth.toFloat, dom.window.innerHeight.toFloat)
    ctx.fillStyle = "rgb(250, 250, 250)"
    ctx.textAlign = "left"
    ctx.textBaseline = "top"
    ctx.font = "36px Helvetica"
    ctx.fillText(s"您已经死亡,被玩家 ${killer} 所杀", 150, 180)
    ctx.fillText(s"Press space to restart", 150, 300)
    ctx.restore()
  }

}
