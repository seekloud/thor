package org.seekloud.thor.shared.ptcl.thor.draw

import org.seekloud.thor.shared.ptcl.model.Point
import org.seekloud.thor.shared.ptcl.thor.ThorSchemaClientImpl

trait FpsRender {
  this:ThorSchemaClientImpl =>

  private var lastRenderTime = System.currentTimeMillis()
  private var lastRenderTimes = 0
  private var renderTimes = 0

  private def addFps() = {
    val time = System.currentTimeMillis()
    renderTimes += 1
    if (time - lastRenderTime > 1000){
      lastRenderTime = time
      lastRenderTimes = renderTimes
      renderTimes = 0
    }
  }


  def drawNetInfo(networkLatency:Long, drawTime: Long, frameTime: Long) = {
    addFps()
    ctx.setFont("Helvetica", baseFont * 15)
    ctx.setTextAlign("start")
    ctx.setFill("#ffffff")
    val fpsStr =  s"fps   : $lastRenderTimes"
    val pingStr = s"ping : ${networkLatency}ms"
    val drawStr = s"drawTime : ${drawTime}ms"
    val frameStr = s"frameTime : ${frameTime}ms"
    ctx.setTextBaseLine("top")
    ctx.fillText(pingStr,window.x * 0.9,10)
    ctx.fillText(fpsStr,window.x * 0.9,30)
    ctx.fillText(drawStr,window.x * 0.9,50)
    ctx.fillText(frameStr,window.x * 0.9,70)
  }
}
