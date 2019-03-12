/*
 * Copyright 2018 seekloud (https://github.com/seekloud)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.seekloud.thor.shared.ptcl.thor.draw

import org.seekloud.thor.shared.ptcl.model.Point
import org.seekloud.thor.shared.ptcl.thor.ThorSchemaClientImpl

trait FpsRender {
  this:ThorSchemaClientImpl =>

  private var lastRenderTime = System.currentTimeMillis()
  var lastRenderTimes = 0
  private var renderTimes = 0

  def addFps() = {
    val time = System.currentTimeMillis()
    renderTimes += 1
    if (time - lastRenderTime > 1000){
      lastRenderTime = time
      lastRenderTimes = renderTimes
      renderTimes = 0
    }
  }


  def drawNetInfo(networkLatency:Long, drawTime: Long, frameTime: Long, peopleNum: Int) = {
    addFps()
    ctx.setFont("Helvetica", baseFont * 15)
    ctx.setTextAlign("start")
    ctx.setFill("#ffffff")
    val fpsStr =  s"fps   : $lastRenderTimes"
    val pingStr = s"ping : ${networkLatency}ms"
    val drawStr = s"drawTime : ${drawTime}ms"
    val frameStr = s"frameTime : ${frameTime}ms"
    val peopleNumStr = s"总人数 : $peopleNum"
    val explainStr2 = s"左键挥刀"
    val explainStr1 = s"右键加速"
    ctx.setTextBaseLine("top")
    ctx.fillText(pingStr,window.x * 0.9,10)
    ctx.fillText(fpsStr,window.x * 0.9,30)
    ctx.fillText(drawStr,window.x * 0.9,50)
    ctx.fillText(frameStr,window.x * 0.9,70)
    ctx.fillText(peopleNumStr,window.x * 0.9,100)
    ctx.setFont("Helvetica", baseFont * 19, "bold")
    ctx.setTextAlign("start")
    ctx.setFill("#ffffff")
    ctx.fillText(explainStr1,window.x * 0.9,130)
    ctx.fillText(explainStr2,window.x * 0.9,155)
  }
}
