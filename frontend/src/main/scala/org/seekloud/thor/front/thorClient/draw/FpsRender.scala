package org.seekloud.thor.front.thorClient.draw

import org.seekloud.thor.front.thorClient.ThorSchemaClientImpl

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

  def drawNetInfo(networkLatency:Long) = {
    addFps()
    ctx.font = "25px Helvetica"
    ctx.textAlign = "start"
    ctx.fillStyle = "white"
    val fpsStr =  s"fps   : $lastRenderTimes"
    val pingStr = s"ping : ${networkLatency}ms"
    ctx.fillText(pingStr,300,10)
    ctx.fillText(fpsStr,300,40)
  }
}
