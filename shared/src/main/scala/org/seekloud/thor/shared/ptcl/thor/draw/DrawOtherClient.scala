package org.seekloud.thor.shared.ptcl.thor.draw
import org.seekloud.thor.shared.ptcl.component.Adventurer
import org.seekloud.thor.shared.ptcl.thor.ThorSchemaClientImpl

import scala.collection.mutable
trait DrawOtherClient {this: ThorSchemaClientImpl =>


  private val barLength = 500
  private val barHeight = 25
  private val r = barHeight/2
  private def barLeft = (canvasSize.x - barLength)/2
  private def barTop = canvasSize.y - barHeight - 50
  
  def drawEnergyBar(adventurer: Adventurer): Unit = {
    ctx.save()

    ctx.setFill("#FFFFFF")
    drawArcRect(r, barLength, barHeight, barLeft, barTop)

    ctx.setFill("#FF0000")
    val preLevel = if(adventurer.level == 1) 0 else config.getMaxEnergyByLevel((adventurer.level - 1).toByte)
    val nowLevel = config.getMaxEnergyByLevel(adventurer.level)
    val fillLength = math.min((adventurer.energy - preLevel).toFloat / (nowLevel - preLevel) * barLength, barLength)
    drawArcRect(r-1, fillLength-2, barHeight-2, barLeft+1, barTop+1)

    ctx.setFill("#FFFFFF")
    ctx.setTextAlign("center")
    ctx.setFont("Comic Sans Ms", 36)
    ctx.setTextBaseLine("top")
    ctx.fillText(adventurer.level.toString, barLeft - 32, barTop)
    ctx.restore()

    ctx.restore()
  }

  def drawArcRect(r: Double, width: Double, height: Double, left: Double, top: Double): Unit = {
    if(width >= 2 * r){
      ctx.save()
      ctx.beginPath()
      ctx.arc(left + r, top + r, r, 0.5 * math.Pi, 1.5 * math.Pi, false)
      ctx.lineTo(left + width - r, top)
      ctx.arc(left + width - r, top + r, r, 1.5 * math.Pi, 0.5 * math.Pi, false)
      ctx.lineTo(left + r, top + height)
      ctx.closePath()
      ctx.fill()
      ctx.restore()
    }
  }

}
