package com.neo.sk.thor.front.thorClient.draw
import com.neo.sk.thor.front.common.Routes
import com.neo.sk.thor.front.thorClient.ThorSchemaClientImpl
import com.neo.sk.thor.shared.ptcl.`object`.Adventurer
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLElement
import org.scalajs.dom.html
import com.neo.sk.thor.shared.ptcl.model.{Point, Score}
import org.scalajs.dom.ext.Color

import scala.collection.mutable
trait DrawOtherClient {this: ThorSchemaClientImpl =>
  private val mapImg = dom.document.createElement("img").asInstanceOf[html.Image]
  mapImg.setAttribute("src", s"${Routes.base}/static/img/map.jpg")
  private val rankImg = dom.document.createElement("img").asInstanceOf[html.Image]
  rankImg.setAttribute("src",s"${Routes.base}/static/img/rank.png")

  def drawBackground(offset: Point, canvasUnit: Int, canvasBoundary: Point):Unit = {
    ctx.save()
//    val pat = ctx.createPattern(mapImg,"repeat")
//    ctx.fillStyle = pat
//    ctx.fillRect(offset.x * canvasUnit,offset.y * canvasUnit,config.boundary.x * canvasUnit ,config.boundary.y * canvasUnit)
//    ctx.fill()
    ctx.fillStyle = "#171b1f"
    ctx.fillRect(0, 0, canvasBoundary.x * canvasUnit, canvasBoundary.y * canvasUnit)
    ctx.fill()
    ctx.drawImage(mapImg, offset.x * canvasUnit, offset.y * canvasUnit, config.boundary.x * canvasUnit, config.boundary.y * canvasUnit)
    ctx.restore()
  }

  private val bar = dom.document.createElement("img").asInstanceOf[html.Image]
  bar.setAttribute("src", s"${Routes.base}/static/img/weaponlevelbar_bg-sheet0.png")
  private val fillBar = dom.document.createElement("img").asInstanceOf[html.Image]
  fillBar.setAttribute("src", s"${Routes.base}/static/img/weaponlevelbar_fg-sheet0.png")

  private val barLength = 500
  private val barHeight = barLength / 288 * 70
  private val barLeft = (dom.window.innerWidth - barLength)/2
  private val barTop = dom.window.innerHeight - barHeight - 20
  def drawEnergyBar(adventurer: Adventurer): Unit = {
    //fixme 垃圾代码
    ctx.save()
    ctx.drawImage(bar, barLeft, barTop, barLength, barHeight )
    ctx.fillStyle = "#ffffff"
    ctx.textAlign = "center"
    ctx.fillText(adventurer.level.toString, barLeft + 32, barTop + 26)
    ctx.font = "48px Comic Sans Ms"
    ctx.restore()
//    println(s"${fillLength}")
    val offsetL = 128
    val offsetT = barHeight / 3
    val fillMax = barLength-offsetL - 8

    val preLevel = if(adventurer.level == 1) 0 else config.getMaxEnergyByLevel(adventurer.level - 1)
    val nowLevel = config.getMaxEnergyByLevel(adventurer.level)
    val fillLength = (adventurer.energy - preLevel).toFloat / (nowLevel - preLevel) * fillMax
    val rateX = fillMax.toFloat/fillBar.width
    ctx.drawImage(fillBar, (fillMax-fillLength)/rateX,0, fillLength/rateX, fillBar.height , barLeft+offsetL, barTop+offsetT, fillLength, barHeight - offsetT*2 - 1)
  }

  def drawBarrage(s:String,x:Double,y:Double):Unit={
    ctx.save()
    ctx.font="30px Comic Sans Ms"
    ctx.fillStyle=Color.White.toString()
    ctx.fillText(s,x,y)
    ctx.restore()
  }

  def drawGameLoading(): Unit = {
    ctx.fillStyle = Color.Black.toString()
    ctx.fillRect(0, 0, dom.window.innerWidth, dom.window.innerHeight)
    ctx.fillStyle = "rgb(250, 250, 250)"
    ctx.textAlign = "left"
    ctx.textBaseline = "top"
    ctx.font = "36px Helvetica"
    ctx.fillText("请稍等，正在连接服务器", 150, 180)
  }

  def drawGameStop(killer: String): Unit = {
    ctx.fillStyle = Color.Black.toString()
    ctx.fillRect(0, 0, dom.window.innerWidth.toFloat, dom.window.innerHeight.toFloat)
    ctx.fillStyle = "rgb(250, 250, 250)"
    ctx.textAlign = "left"
    ctx.textBaseline = "top"
    ctx.font = "36px Helvetica"
    ctx.fillText(s"您已经死亡,被玩家 ${killer} 所杀", 150, 180)
    ctx.fillText(s"Press space to restart", 150, 300)
    println()
  }


  def drawTextLine(str: String, x: Int, lineNum: Int, lineBegin: Int = 0,`type`:Int) = {
    ctx.textAlign = "left"
    ctx.textBaseline = "top"
    `type` match{
      case 3 =>
        ctx.font = "16px Comic Sans MS"
      case _ =>
        ctx.font = "18px 黑体"
        ctx.fillStyle="black"

    }
    ctx.fillText(str, x, (lineNum + lineBegin - 1) * 14)
  }

  def drawRank(Rank:List[Score],CurrentOrNot:Boolean,id:String):Unit={
    val num=Rank.size
    val RankBaseLine = 2
    var index = 0
    var x=10
    var yourNameIn = false
    var text="  排行榜"
    if (!CurrentOrNot){
      x=dom.document.documentElement.clientWidth - 260
      text="历史排行榜"
    }
    ctx.fillStyle="rgba(192,192,192,0.6)"
    ctx.drawImage(rankImg,x,0,250,250)
//    drawTextLine(s"       ${text}        ", x+40, index, RankBaseLine+2,2)
    ctx.beginPath()
    ctx.fillStyle="white"
    Rank.foreach { score =>
      if (index<5){
        index += 1
        if (score.id == id)
          yourNameIn = true
        drawTextLine(s" $index:  ${score.n.take(5)}    score=${score.e}   kill=${score.k}", x+10, index*2, RankBaseLine,3)
      }
    }
    index+=1
    if (!yourNameIn){
      ctx.fillStyle="#FFFF00"
      val yourScore = Rank.find(_.id == id).get
      drawTextLine(s" $index:  ${yourScore.n.take(5)}    score=${yourScore.e}   kill=${yourScore.k}", x+10, index*2, RankBaseLine+3,3)
    }

  }

}
