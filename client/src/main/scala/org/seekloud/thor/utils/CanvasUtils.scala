package org.seekloud.thor.utils

import javafx.scene.image.Image
import org.seekloud.thor.shared.ptcl.model.Point
import org.seekloud.thor.shared.ptcl.util.middleware.{MiddleCanvas, MiddleContext, MiddleFrame}

/**
  * User: Jason
  * Date: 2019/3/10
  * Time: 15:03
  */
object CanvasUtils {

  def rotateImage(typ: String, drawFrame: MiddleFrame, ctx: MiddleContext, preCanvas: List[MiddleCanvas] = Nil, preImage: List[Image] = Nil, src:String, position: Point, offset:Point,
    width: Float, height: Float, angle: Float, preTime: Long, level: Int): Unit = {
    //position 旋转的中心点
    //offset 图片中心点距离旋转中心点的偏移量（以自身旋转则为Point(0,0)）
    //width 图片的渲染宽度
    //height 图片的渲染高度 如果为0 则根据宽度等比例缩放
    //angle 旋转角度

    //    val img = drawFrame.createImage(src)
    //    val drawHeight: Float = if(height == 0) width else height
    //      if(height == 0) width / imgWidth.toFloat * imgHeight.toFloat else height

    ctx.save()
    ctx.translate(position.x, position.y)
    ctx.rotate(angle)
    preImage match {
      case Nil =>
        preCanvas match {
          case Nil =>
            val img = drawFrame.createImage(src)
            val imgWidth = img.width
            val imgHeight = img.height
            val drawHeight: Float = if(height == 0) width / imgWidth.toFloat * imgHeight.toFloat else height
            ctx.drawImage(img, -width/2 + offset.x, -drawHeight/2 + offset.y, Some(width, drawHeight))
          case _ =>
            val imgWidth = preCanvas.head.getWidth()
            val imgHeight = preCanvas.head.getHeight()
            val drawHeight: Float = if(height == 0) width / imgWidth.toFloat * imgHeight.toFloat else height
            typ match {
              case "adventurer" => drawAdventurerByPreCanvas(ctx, preCanvas, level, Point(-width/2 + offset.x, -drawHeight/2 + offset.y), Point(width, drawHeight))
              case "weapon" => drawWeaponByPreCanvas(ctx, preCanvas, level, Point(-width/2 + offset.x, -drawHeight/2 + offset.y), Point(width, drawHeight))
              case _ => ()
            }
            if(System.currentTimeMillis() - preTime < 2000) {
              //              val img = drawFrame.createImage(src)
              //              ctx.drawImage(img, -width / 2 + offset.x, -drawHeight / 2 + offset.y, Some(width, drawHeight))
            }
            else{

            }

        }
      case _ =>
        typ match {
          case "adventurer" =>
            val imgWidth = preImage((level - 1) % 20).getWidth
            val imgHeight = preImage((level - 1) % 20).getHeight
            val drawHeight: Float = if(height == 0) width / imgWidth.toFloat * imgHeight.toFloat else height
            val a = System.currentTimeMillis()
            drawAdventurerByPreImage(ctx, preImage, level, Point(-width/2 + offset.x, -drawHeight/2 + offset.y), Point(width, drawHeight))
            val b = System.currentTimeMillis()
            if (b-a>5) println(s"draw single adv time span: ${b-a}")
          case "weapon" =>
            val imgWidth = preImage((level - 1) / 4).getWidth
            val imgHeight = preImage((level - 1) / 4).getHeight
            val drawHeight: Float = if(height == 0) width / imgWidth.toFloat * imgHeight.toFloat else height
            drawWeaponByPreImage(ctx, preImage, level, Point(-width/2 + offset.x, -drawHeight/2 + offset.y), Point(width, drawHeight))
          case _ => ()
        }
    }
    if(typ != "adventurer" && typ != "weapon") {
      val img = drawFrame.createImage(src)
      val imgWidth = img.width
      val imgHeight = img.height
      val drawHeight: Float = if(height == 0) width / imgWidth.toFloat * imgHeight.toFloat else height
      ctx.drawImage(img, -width / 2 + offset.x, -drawHeight / 2 + offset.y, Some(width, drawHeight))
    }

    // 恢复设置
    ctx.rotate(-angle)
    ctx.translate(-position.x, -position.y)
    // 之后canvas的原点又回到左上角，旋转角度为0
    ctx.restore()
  }

  def drawAdventurerByPreCanvas(ctx: MiddleContext, preCanvasAdventurer: List[MiddleCanvas],  level: Int, offset: Point, size: Point): Unit ={

    ctx.save()
    ctx.drawImage(preCanvasAdventurer((level-1) % 20), offset.x, offset.y, Some(size.x, size.y))
    ctx.restore()

  }

  def drawAdventurerByPreImage(ctx: MiddleContext, preImageAdventurer: List[Image],  level: Int, offset: Point, size: Point): Unit ={

    ctx.save()
    ctx.drawImage(preImageAdventurer((level - 1) % 20), offset.x, offset.y, Some(size.x, size.y))
    ctx.restore()

  }

  def drawWeaponByPreCanvas(ctx: MiddleContext, preCanvasWeapon: List[MiddleCanvas], level: Int, offset: Point, size: Point): Unit ={

    ctx.save()
    ctx.drawImage(preCanvasWeapon((level-1) / 4), offset.x, offset.y, Some(size.x, size.y))
    ctx.restore()

  }
  def drawWeaponByPreImage(ctx: MiddleContext, preImageWeapon: List[Image], level: Int, offset: Point, size: Point): Unit ={

    ctx.save()
    ctx.drawImage(preImageWeapon((level - 1) / 4), offset.x, offset.y, Some(size.x, size.y))
    ctx.restore()

  }

  def rotateColor(typ: String, drawFrame: MiddleFrame, ctx: MiddleContext, preImage: List[Image] = Nil, src:String, position: Point, offset:Point,
    width: Float, height: Float, angle: Float, level: Int, color: String, radius: Float, canvasUnit: Float): Unit = {

    ctx.save()
    ctx.translate(position.x, position.y)
    ctx.rotate(angle)
    preImage match {
      case Nil =>
        val img = drawFrame.createImage(src)
        val imgWidth = img.width
        val imgHeight = img.height
        val drawHeight: Float = if(height == 0) width / imgWidth.toFloat * imgHeight.toFloat else height
        ctx.setFill(color)
        ctx.beginPath()
        ctx.arc(offset.x, offset.y, radius * canvasUnit, 0, 360, counterclockwise = false)
        ctx.closePath()
        ctx.fill()
//        ctx.drawImage(img, -width/2 + offset.x, -drawHeight/2 + offset.y, Some(width, drawHeight))
      case _ =>
        typ match {
          case "adventurer" =>
            val imgWidth = preImage((level - 1) % 20).getWidth
            val imgHeight = preImage((level - 1) % 20).getHeight
            val drawHeight: Float = if(height == 0) width / imgWidth.toFloat * imgHeight.toFloat else height
            val a = System.currentTimeMillis()
            ctx.setFill(color)
            ctx.beginPath()
            ctx.arc( offset.x, offset.y, radius * canvasUnit, 0, 360, counterclockwise = false)
            ctx.closePath()
            ctx.fill()
//            drawAdventurerByPreImage(ctx, preImage, level, Point(-width/2 + offset.x, -drawHeight/2 + offset.y), Point(width, drawHeight))
            val b = System.currentTimeMillis()
            if (b-a>5) println(s"draw single adv time span: ${b-a}")

          case _ => ()
        }
    }

    ctx.rotate(-angle)
    ctx.translate(-position.x, -position.y)
    ctx.restore()
  }

}
