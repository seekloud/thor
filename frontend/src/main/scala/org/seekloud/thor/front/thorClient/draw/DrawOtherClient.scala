package org.seekloud.thor.front.thorClient.draw
import org.seekloud.thor.front.common.Routes
import org.seekloud.thor.front.thorClient.ThorSchemaClientImpl
import org.seekloud.thor.shared.ptcl.component.Adventurer
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLElement
import org.scalajs.dom.html
import org.seekloud.thor.shared.ptcl.model.{Point, Score}
import org.scalajs.dom.ext.Color

import scala.collection.mutable
trait DrawOtherClient {this: ThorSchemaClientImpl =>

  private val bar = dom.document.createElement("img").asInstanceOf[html.Image]
  bar.setAttribute("src", s"${Routes.base}/static/img/weaponlevelbar_bg-sheet0.png")
  private val fillBar = dom.document.createElement("img").asInstanceOf[html.Image]
  fillBar.setAttribute("src", s"${Routes.base}/static/img/weaponlevelbar_fg-sheet0.png")

  private val barLength = 500
  private val barHeight = barLength / 288 * 70 //这张图片的比例是288 * 70
  private val barLeft = (dom.window.innerWidth - barLength)/2
  private val barTop = dom.window.innerHeight - barHeight - 20
  
  def drawEnergyBar(adventurer: Adventurer): Unit = {
    ctx.save()
    //画能量条背景
    ctx.drawImage(bar, barLeft, barTop, barLength, barHeight )

    //等级
    ctx.fillStyle = "#ffffff"
    ctx.textAlign = "center"
    ctx.font = "36px Comic Sans Ms"
    ctx.fillText(adventurer.level.toString, barLeft + 32, barTop + 8)
    ctx.restore()

    //画能量条进度
    val offsetLeft = 128
    val offsetTop = barHeight / 3 //开始画进度条的位置和能量条背景的偏移量
    val fillMax = barLength-offsetLeft - 8 //进度条的最大长度（px）
    val rate = fillMax.toFloat/fillBar.width //缩放比例

    //当前能量值占该等级的比值
    val preLevel = if(adventurer.level == 1) 0 else config.getMaxEnergyByLevel(adventurer.level - 1)
    val nowLevel = config.getMaxEnergyByLevel(adventurer.level)
    val fillLength = math.min((adventurer.energy - preLevel).toFloat / (nowLevel - preLevel) * fillMax, fillMax)

    ctx.drawImage(fillBar, (fillMax-fillLength)/rate,0, fillLength/rate, fillBar.height , barLeft+offsetLeft, barTop+offsetTop, fillLength, barHeight - offsetTop*2 - 1)
  }
}
