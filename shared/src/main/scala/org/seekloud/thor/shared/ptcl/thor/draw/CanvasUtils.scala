package org.seekloud.thor.shared.ptcl.thor.draw

import org.seekloud.thor.shared.ptcl.model.Point
import org.seekloud.thor.shared.ptcl.util.middleware.{MiddleContext, MiddleFrame}

/**
  * @author Jingyi
  * @version 创建时间：2018/11/20
  */
object CanvasUtils {
  
  def rotateImage(drawFrame: MiddleFrame, ctx: MiddleContext, src:String, position: Point, offset:Point,
                  width: Float, height: Float, angle: Float) = {
    //position 旋转的中心点
    //offset 图片中心点距离旋转中心点的偏移量（以自身旋转则为Point(0,0)）
    //width 图片的渲染宽度
    //height 图片的渲染高度 如果为0 则根据宽度等比例缩放
    //angle 旋转角度

    val img = drawFrame.createImage(src)
    val imgWidth = img.width
    val imgHeight = img.height
    val drawHeight = if(height == 0) width / imgWidth * imgHeight else height

    ctx.save()
    ctx.translate(position.x, position.y)
    ctx.rotate(angle)
    ctx.drawImage(img, -width/2 + offset.x, -drawHeight/2 + offset.y, Some(width, drawHeight))
    // 恢复设置
    ctx.rotate(-angle)
    ctx.translate(-position.x, -position.y)
    // 之后canvas的原点又回到左上角，旋转角度为0
    ctx.restore()
  }
}
