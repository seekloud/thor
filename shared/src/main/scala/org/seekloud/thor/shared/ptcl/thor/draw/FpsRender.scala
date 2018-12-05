package org.seekloud.thor.shared.ptcl.thor.draw

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

  def drawNetInfo(networkLatency:Long) = {
    addFps()
    ctx.setFont("Helvetica", 25)
    ctx.setTextAlign("start")
    ctx.setFill("#ffffff")
    val fpsStr =  s"fps   : $lastRenderTimes"
    val pingStr = s"ping : ${networkLatency}ms"
    ctx.setTextBaseLine("top")
    ctx.fillText(pingStr,300,10)
    ctx.fillText(fpsStr,300,40)
  }
}
