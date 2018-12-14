package org.seekloud.thor.shared.ptcl.thor.draw
import org.seekloud.thor.shared.ptcl.component.Adventurer
import org.seekloud.thor.shared.ptcl.model.{Point, Score}
import org.seekloud.thor.shared.ptcl.thor.ThorSchemaClientImpl

import scala.collection.mutable
trait DrawOtherClient {this: ThorSchemaClientImpl =>

  private val bar = drawFrame.createImage("/img/weaponlevelbar_bg-sheet0.png")
  private val fillBar = drawFrame.createImage("/img/weaponlevelbar_fg-sheet0.png")

  private val barLength = 500
  private val barHeight = barLength / 288 * 70 //这张图片的比例是288 * 70
  private def barLeft = (canvasSize.x - barLength)/2
  private def barTop = canvasSize.y - barHeight - 20
  
  def drawEnergyBar(adventurer: Adventurer): Unit = {
    ctx.save()
    //画能量条背景
    ctx.drawImage(bar, barLeft, barTop, Some(barLength, barHeight) )

    //等级
    ctx.setFill("#ffffff")
    ctx.setTextAlign("center")
    ctx.setFont("Comic Sans Ms", 36)
    ctx.setTextBaseLine("top")
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

    ctx.drawImage(fillBar , barLeft+offsetLeft, barTop+offsetTop, Some(fillLength, barHeight - offsetTop*2 - 1), Some((fillMax-fillLength)/rate), Some(0), Some(fillLength/rate, fillBar.height))
  }

  def drawAttacking(offset: Point, adventurer: Adventurer, step: Int) = {
//    println(adventurer.direction + "dudu")
//    println("drawAttacking" + step)
    val x = adventurer.getPosition.x + offset.x
    val y = adventurer.getPosition.y + offset.y
    val rx = adventurer.radius * canvasUnit * 0.85
    val sA = adventurer.direction - scala.math.Pi * 2.0 / 9.0 * step - scala.math.Pi * 2.0 / 9.0
    val eA = adventurer.direction - scala.math.Pi * 2.0 / 9.0 * step + scala.math.Pi * 2.0 / 9.0
    ctx.save()
    ctx.beginPath()
    ctx.setStrokeStyle("#30B8E8")
    ctx.lineWidth(config.getWeaponLengthByLevel(adventurer.weaponLevel) * canvasUnit * 0.85)
    ctx.arc(x * canvasUnit, y * canvasUnit, rx + config.getWeaponLengthByLevel(adventurer.weaponLevel) / 2 * canvasUnit * 0.85, sA, eA, false)
    ctx.stroke()
    ctx.restore()
  }
}
