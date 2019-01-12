package org.seekloud.thor.shared.ptcl.thor.draw

import org.seekloud.thor.shared.ptcl.component.Adventurer
import org.seekloud.thor.shared.ptcl.model.Point
import org.seekloud.thor.shared.ptcl.util.middleware.{MiddleCanvas, MiddleContext, MiddleFrame}

/**
  * @author Jingyi
  * @version 创建时间：2018/11/20
  */
object CanvasUtils {
  
  def rotateImage(typ: String, drawFrame: MiddleFrame, ctx: MiddleContext, preCanvasAdventurer: List[MiddleCanvas] = Nil, src:String, position: Point, offset:Point,
                  width: Float, height: Float, angle: Float, preTime: Long, level: Int) = {
    //position 旋转的中心点
    //offset 图片中心点距离旋转中心点的偏移量（以自身旋转则为Point(0,0)）
    //width 图片的渲染宽度
    //height 图片的渲染高度 如果为0 则根据宽度等比例缩放
    //angle 旋转角度

    val img = drawFrame.createImage(src)
    val imgWidth = img.width
    val imgHeight = img.height
    val drawHeight: Float = if(height == 0) width / imgWidth.toFloat * imgHeight.toFloat else height

    ctx.save()
    ctx.translate(position.x, position.y)
    ctx.rotate(angle)
    if(typ != "adventurer")
      ctx.drawImage(img, -width/2 + offset.x, -drawHeight/2 + offset.y, Some(width, drawHeight))
    else
      preCanvasAdventurer match {
        case Nil =>
          ctx.drawImage(img, -width/2 + offset.x, -drawHeight/2 + offset.y, Some(width, drawHeight))
        case _ =>
          if(System.currentTimeMillis() - preTime < 12000)
            ctx.drawImage(img, -width/2 + offset.x, -drawHeight/2 + offset.y, Some(width, drawHeight))
          else
            drawAdventurerByPre(ctx, preCanvasAdventurer, level, Point(-width/2 + offset.x, -drawHeight/2 + offset.y), Point(width, drawHeight))
      }
    // 恢复设置
    ctx.rotate(-angle)
    ctx.translate(-position.x, -position.y)
    // 之后canvas的原点又回到左上角，旋转角度为0
    ctx.restore()
  }

  def drawAdventurerByPre(ctx: MiddleContext, preCanvasAdventurer: List[MiddleCanvas], level: Int, offset: Point, size: Point): Unit ={

    ctx.save()
    ctx.drawImage(preCanvasAdventurer((level-1) % 20), offset.x, offset.y, Some(size.x, size.y))
    ctx.restore()

  }
}
