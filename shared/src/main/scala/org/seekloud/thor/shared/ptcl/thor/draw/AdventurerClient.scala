package org.seekloud.thor.shared.ptcl.thor.draw

import org.seekloud.thor.shared.ptcl
import org.seekloud.thor.shared.ptcl.component.Adventurer
import org.seekloud.thor.shared.ptcl.model.Point
import org.seekloud.thor.shared.ptcl.thor.ThorSchemaClientImpl

import scala.collection.mutable


/**
  * Created by Jingyi on 2018/11/9

  */

trait AdventurerClient { this: ThorSchemaClientImpl =>

//  private  val mapImg = dom.document.createElement("img").asInstanceOf[html.Image]
//  mapImg.setAttribute("src", s"${Routes.base}/img/logo-sheet0.png")

  private val adventurerCanvasCacheMap = mutable.HashMap[(Byte, Boolean), Any]()

  def getMoveDistance(adventurer: Adventurer, offSetTime: Long): Point = {
    // 获取当前渲染帧与逻辑帧的偏移量
    val r = config.getAdventurerRadiusByLevel(adventurer.level)
    val position = adventurer.getAdventurerState.position
    var moveDistance = Point(0, 0)

    if(adventurer.isMove){
      moveDistance = config.getMoveDistanceByFrame(adventurer.getAdventurerState.speedLevel).rotate(adventurer.getAdventurerState.direction) * offSetTime.toFloat / config.frameDuration
      //如果达到边界 则不再往外走
      val delay = 0.5
      if(position.x - r < delay || position.x + r > config.boundary.x - delay) moveDistance = moveDistance.copy(x = 0)
      if(position.y - r < delay || position.y + r > config.boundary.y - delay) moveDistance = moveDistance.copy(y = 0)
    }

    moveDistance
  }

  def drawAdventurer(offSetTime: Long, offset: Point, canvasUnit: Float): Unit ={

    def drawAnAdventurer(adventurer: Adventurer) = {

      val r = config.getAdventurerRadiusByLevel(adventurer.level)
      val position = adventurer.getAdventurerState.position
      val moveDistance = getMoveDistance(adventurer, offSetTime)
      
      val sx = position.x + offset.x + moveDistance.x
      val sy = position.y + offset.y + moveDistance.y
      val dx = 2 * r
      val dy = 2 * r
      
      if(adventurer.isSpeedUp){ //加速特效
        val height = config.getAdventurerRadiusByLevel(adventurer.level) * 2 * canvasUnit
        val width = 3*height

        CanvasUtils.rotateImage(drawFrame, ctx, s"/img/speedparticles.png",Point(sx, sy) * canvasUnit, Point(-height, 0), width, height, adventurer.getAdventurerState.direction)
      }

      //画人物
//      val drawX = if(systemFrame%6 < 3) dx * 0.98.toFloat else dx
      CanvasUtils.rotateImage(drawFrame, ctx, s"/img/Adventurer-${adventurer.level}.png", Point(sx, sy) * canvasUnit, Point(0, 0), dx * canvasUnit * 0.85.toFloat, 0, adventurer.getAdventurerState.direction)

      //画武器
      var step = 3
      var isAttacking = false
      attackingAdventureMap.filterNot(_._2 < 0).get(adventurer.playerId) match{
        case Some(s) =>
          step = s
          if(s != 0) isAttacking = true
        case _ =>
      }
      val weaponLength = config.getWeaponLengthByLevel(adventurer.getAdventurerState.level)
      val angle = adventurer.getAdventurerState.direction - (step * math.Pi / 3 + 1/12 * math.Pi).toFloat //武器旋转角度
      val gap:Float = 1 // 武器离人物的距离
      val move: Float = if(isAttacking) math.Pi.toFloat * 1 / 3 * offSetTime.toFloat / config.frameDuration else 0 //该渲染帧的角度偏移量，攻击中禁止移动
      val weaponPosition = Point(sx, sy) + Point(-r - gap.toFloat, gap + weaponLength/2).rotate(angle + move - math.Pi.toFloat/2)

      CanvasUtils.rotateImage(drawFrame, ctx, s"/img/weapon-${adventurer.level}.png", weaponPosition * canvasUnit, Point(0, 0), weaponLength * canvasUnit, 0, angle + move)


      //用户昵称
      ctx.setFill("#ffffff")
      ctx.setTextAlign("center")
      ctx.setFont("微软雅黑", 20)
      ctx.fillText(s"${adventurer.name}", sx * canvasUnit, (sy + dy) * canvasUnit + 20)}
    adventurerMap.foreach{
      adventurer =>
        drawAnAdventurer(adventurer._2)
    }
  }

  def drawDying(offset: Point, offsetTime:Long, canvasUnit: Float): Any = {

    def drawADying(adventurer: Adventurer, step: Int): Any = {

      val position = adventurer.getAdventurerState.position
      val r = config.getAdventurerRadiusByLevel(adventurer.level)

      val o = if(offsetTime.toFloat/config.frameDuration > 0.5) 1 else 0

      //根据进度选取死亡动画 step -> 2,1,0 img -> 1~6

      val img = drawFrame.createImage(s"/img/kill${5 - step*2 + o}.png")

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

    if(adventurer.getAdventurerState.isUpdateLevel){

      val img = drawFrame.createImage(s"/img/level-up.png")

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


  def drawAdventurers(offSetTime: Long, offset: Point, canvasUnit: Float): Unit ={

    drawAdventurer(offSetTime, offset, canvasUnit)
    drawDying(offset, offSetTime, canvasUnit)
    adventurerMap.get(myId).foreach{
      adventurer =>
        drawLevelUp(adventurer, adventurer.getAdventurerState.levelUpExecute, offSetTime, offset, canvasUnit)
    }
  }

}






