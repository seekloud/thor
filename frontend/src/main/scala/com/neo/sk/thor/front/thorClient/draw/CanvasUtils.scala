package com.neo.sk.thor.front.thorClient.draw

import com.neo.sk.thor.shared.ptcl.model.Point
import org.scalajs.dom
import org.scalajs.dom.html
import org.scalajs.dom.html.Image

/**
  * @author Jingyi
  * @version 创建时间：2018/11/20
  */
object CanvasUtils {
  def rotateImage(ctx:dom.CanvasRenderingContext2D, src:String, position: Point, offset:Point, width: Float, height: Float, angle: Float) = {
    val img = dom.document.createElement("img").asInstanceOf[html.Image]
    img.setAttribute("src", src)
    ctx.save()
    ctx.translate(position.x + width/2, position.y + height/2)
    ctx.rotate(angle)
    ctx.drawImage(img, -width/2 + offset.x, -height/2 + offset.y, width, height)
    // 恢复设置（恢复的步骤要跟你修改的步骤向反）
    ctx.rotate(-angle)
    ctx.translate(-(position.x + width/2), -(position.y + height/2))
    // 之后canvas的原点又回到左上角，旋转角度为0
    ctx.restore()
  }
}
