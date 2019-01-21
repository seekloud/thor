package org.seekloud.thor.shared.ptcl.thor.draw

import org.seekloud.thor.shared.ptcl
import org.seekloud.thor.shared.ptcl.component.Adventurer
import org.seekloud.thor.shared.ptcl.model.Point
import org.seekloud.thor.shared.ptcl.thor.ThorSchemaClientImpl
import org.seekloud.thor.shared.ptcl.model.Constants._
import scala.collection.mutable


/**
  * Created by Jingyi on 2018/11/9

  */

trait AdventurerClient { this: ThorSchemaClientImpl =>

//  private val adventurerCanvasCacheMap = mutable.HashMap[(Byte, Boolean), Any]()

  def getMoveDistance(adventurer: Adventurer, offSetTime: Long): Point = {
    // 获取当前渲染帧与逻辑帧的偏移量
    val r = config.getAdventurerRadiusByLevel(adventurer.level)
    val position = adventurer.getAdventurerState.position
    var moveDistance = Point(0, 0)

    if(adventurer.isMove && adventurer.isIntersect == 0){
      moveDistance = config.getMoveDistanceByFrame(adventurer.getAdventurerState.level, adventurer.isSpeedUp).rotate(adventurer.getAdventurerState.direction) * offSetTime.toFloat / config.frameDuration
      //如果达到边界 则不再往外走
      if(position.x - r <= 0 || position.x + r >= config.boundary.x) moveDistance = moveDistance.copy(x = 0)
      if(position.y - r <= 0 || position.y + r >= config.boundary.y) moveDistance = moveDistance.copy(y = 0)
    }
    moveDistance
  }

  def drawAdventurer(offSetTime: Long, offset: Point, canvasUnit: Float, canvasBoundary: Point): Unit ={

    def drawAnAdventurer(adventurer: Adventurer) = {
      val r = config.getAdventurerRadiusByLevel(adventurer.level)
      val position = adventurer.getAdventurerState.position
      val moveDistance = getMoveDistance(adventurer, offSetTime)

      val sx = position.x + offset.x + moveDistance.x
      val sy = position.y + offset.y + moveDistance.y
      val dx = 2 * r
      val dy = 2 * r
      if (0 < sx+r && sx-r < canvasBoundary.x && 0 < sy+r && sy-r < canvasBoundary.y) {
        if (adventurer.isSpeedUp) { //加速特效
          val height = config.getAdventurerRadiusByLevel(adventurer.level) * 2 * canvasUnit
          val width = 3 * height

          CanvasUtils.rotateImage("speed",drawFrame, ctx, Nil, pictureMap("speedParticles.png"), Point(sx, sy) * canvasUnit, Point(-height, 0), width, height, adventurer.getAdventurerState.direction,preTime, adventurer.getAdventurerState.level)
        }

        //画人物
        //      val drawX = if(systemFrame%6 < 3) dx * 0.98.toFloat else dx
        if(ifTest){
          ctx.save()
          ctx.setFill("#FF0000")
          ctx.beginPath()
          ctx.arc(sx * canvasUnit, sy * canvasUnit, r * canvasUnit, 0, 2*Math.PI, false)
          ctx.closePath()
          ctx.fill()
          ctx.restore()
        }
        CanvasUtils.rotateImage("adventurer", drawFrame, ctx, preCanvasAdventurer, pictureMap(s"char${(adventurer.level % 21 - 1)/4 + 1}-${(adventurer.level - 1) % 4}.png"), Point(sx, sy) * canvasUnit, Point(0, 0), dx * canvasUnit * 0.95.toFloat, 0, adventurer.getAdventurerState.direction,preTime, adventurer.getAdventurerState.level)
//        println(s"arc:${r * canvasUnit} img:${dx * canvasUnit * 0.85.toFloat}")
        //出生保护
        newbornAdventurerMap.get(adventurer.playerId) match {
          case Some(s) =>
            ctx.save()
            ctx.setFill("rgba(79,148,205,0.4)")
            ctx.setShadowColor("rgb(255,255,255)")
            ctx.beginPath()
            ctx.arc(sx * canvasUnit, sy * canvasUnit, r * canvasUnit * 1.15, 0, 2 * Math.PI,false)
            ctx.closePath()
            ctx.fill()
            ctx.restore()
          case _ =>
        }
        //画武器
        var step:Float = 3
        var isAttacking = false
        attackingAdventureMap.filterNot(_._2 < 0).get(adventurer.playerId) match {
          case Some(s) =>
            step = s
            if(step != 0)isAttacking = true
          case _ =>
        }
        val weaponLength = config.getWeaponLengthByLevel(adventurer.getAdventurerState.level)
        val angle = adventurer.getAdventurerState.direction - math.Pi.toFloat * (3*step + 1) / 10  //武器旋转角度
        val gap: Float = 0 // 武器离人物的距离
        val move: Float = if (isAttacking) math.Pi.toFloat * 3 / 10 * offSetTime.toFloat / config.frameDuration else 0 //该渲染帧的角度偏移量，攻击中禁止移动
        val weaponPosition = Point(sx, sy) + Point(weaponLength / 2, r).rotate(angle + move)
        CanvasUtils.rotateImage("weapon", drawFrame, ctx, preCanvasWeapon, pictureMap(s"weapon${(adventurer.level-1)/4+1}.png"), weaponPosition * canvasUnit, Point(0, 0), weaponLength * canvasUnit, 0, angle + move,preTime, adventurer.getAdventurerState.level)

        //用户昵称
        ctx.save()
        ctx.setFill("#ffffff")
        ctx.setTextAlign("center")
        ctx.setFont("微软雅黑", 20)
        ctx.setTextBaseLine("top")
        ctx.fillText(s"${adventurer.name}", sx * canvasUnit, (sy + r) * canvasUnit + 20)
        ctx.restore()
      }
    }
    adventurerMap.foreach{
      adventurer =>
        if (!dyingAdventurerMap.contains(adventurer._1)) {
          drawAnAdventurer(adventurer._2)
        }
    }
  }

  def drawDying(offset: Point, offsetTime:Long, canvasUnit: Float): Any = {

    def drawADying(adventurer: Adventurer, step: Int): Any = {

      val position = adventurer.getAdventurerState.position
      val r = config.getAdventurerRadiusByLevel(adventurer.level)

      val o = if(offsetTime.toFloat/config.frameDuration > 0.5) 1 else 0

      //根据进度选取死亡动画 step -> 2,1,0 img -> 1~6

      val img = drawFrame.createImage(pictureMap(s"kill${5 - step*2 + o}.png"))
//      println(s"dying img: /img/kill${5 - step*2 + o}.png")

      val width = img.width
      val height = img.height

//      val canvasCache = drawFrame.createCanvas(math.ceil(width * canvasUnit).toInt, math.ceil(height * canvasUnit).toInt)
//      val ctxCache = canvasCache.getCtx

      ctx.drawImage(img, (position.x + offset.x) * canvasUnit - width/2, (position.y + offset.y) * canvasUnit - height/2)
//      canvasCache.change2Image()
    }

    dyingAdventurerMap.foreach{
      adventurer =>
        drawADying(adventurer._2._1, adventurer._2._2)
    }
  }

  def drawLevelUp(adventurer: Adventurer, step: Int, offSetTime: Long, offset:Point, canvasUnit: Float) = {

    if(adventurer.isUpdateLevel){

      val img = drawFrame.createImage(pictureMap("level-up.png"))

      val width = img.width
      val height = img.height
      val position = adventurer.getAdventurerState.position
      val r = config.getAdventurerRadiusByLevel(adventurer.level)
      val moveDistance = getMoveDistance(adventurer, offSetTime)

      val movePerStep = 15 //每个逻辑帧的高度位移
      val offsetHeight =  - 200 + movePerStep * (step - offSetTime.toFloat/config.frameDuration) //每个渲染帧的高度偏移量
      val opacity = 0.1 * step //透明度逐渐增加

      ctx.save()
      ctx.setGlobalAlpha(opacity)
      ctx.drawImage(img, (position.x + offset.x + moveDistance.x) * canvasUnit - width/2, (position.y + offset.y + moveDistance.y - r) * canvasUnit - height/2 + offsetHeight)
      ctx.restore()
    }
  }


  def drawAdventurers(offSetTime: Long, offset: Point, canvasUnit: Float, canvasBoundary:Point): Unit ={

    drawAdventurer(offSetTime, offset, canvasUnit, canvasBoundary)
    drawDying(offset, offSetTime, canvasUnit)
    adventurerMap.get(myId).foreach{
      adventurer =>
        drawLevelUp(adventurer, adventurer.getAdventurerState.levelUpExecute, offSetTime, offset, canvasUnit)
    }
  }

}






