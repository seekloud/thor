package org.seekloud.thor.front.thorClient.draw

import org.seekloud.thor.front.common.Routes
import org.seekloud.thor.front.thorClient.ThorSchemaClientImpl
import org.seekloud.thor.shared.ptcl
import org.seekloud.thor.shared.ptcl.component.Adventurer
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLElement
import org.scalajs.dom.html
import org.seekloud.thor.shared.ptcl.model.Point

import scala.collection.mutable


/**
  * Created by Jingyi on 2018/11/9

  */

trait AdventurerClient { this: ThorSchemaClientImpl =>

//  private  val mapImg = dom.document.createElement("img").asInstanceOf[html.Image]
//  mapImg.setAttribute("src", s"${Routes.base}/static/img/logo-sheet0.png")

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

  def drawAdventurer(offSetTime: Long, offset: Point, canvasUnit: Int): Unit ={
    //fixme 垃圾代码
    def drawAnAdventurer(adventurer: Adventurer) = {

      val r = config.getAdventurerRadiusByLevel(adventurer.level)
      val position = adventurer.getAdventurerState.position
      val moveDistance = getMoveDistance(adventurer, offSetTime)


      val sx = position.x + offset.x + moveDistance.x
      val sy = position.y + offset.y + moveDistance.y
      val dx = 2 * r
      val dy = 2 * r

      val src = s"/thor/static/img/Adventurer-${adventurer.level}.png"
      val weapon = s"/thor/static/img/weapon-${adventurer.level}.png"

      if(adventurer.isSpeedUp){
        val height = config.getAdventurerRadiusByLevel(adventurer.level) * 2 * canvasUnit
        val width = 3*height

        CanvasUtils.rotateImage(ctx, s"/thor/static/img/speedparticles.png",Point(sx, sy) * canvasUnit, Point(-height, 0), width, height, adventurer.getAdventurerState.direction)
      }

      CanvasUtils.rotateImage(ctx, src, Point(sx, sy) * canvasUnit, Point(0, 0), dx * canvasUnit, 0, adventurer.getAdventurerState.direction)

      var step = 3
      var isAttacting = false
      attackingAdventureMap.get(adventurer.playerId) match{
        case Some(s) =>
          step = s
          if(s!=0)
            isAttacting = true
        case _ =>
      }
      val angle = adventurer.getAdventurerState.direction - (step * math.Pi / 3 + 1/12 * math.Pi).toFloat
      val weaponLength = config.getWeaponLengthByLevel(adventurer.getAdventurerState.level)
      val weaponWidth = 5
      val gap:Float = 1
      val move: Float = if(isAttacting) math.Pi.toFloat * 1 / 3 * offSetTime.toFloat / config.frameDuration else 0
//      println(s"d:${adventurer.direction} angle:${angle} rotate${angle + move + math.Pi.toFloat/2}")
//      println(s"${Point(r+gap, -r)} ${Point(r+gap, -r).rotate(angle + move + math.Pi.toFloat/2)}")
//      CanvasUtils.rotateImage(ctx, weapon, (Point(sx, sy) + Point(r + gap+weaponLength / 2, 0).rotate(angle + move + math.Pi.toFloat/2)) * canvasUnit, Point(0, 0), weaponLength * canvasUnit, weaponWidth * canvasUnit, angle + move)
      CanvasUtils.rotateImage(ctx, weapon, (Point(sx, sy) + Point(-r - gap.toFloat, gap + weaponLength/2).rotate(angle + move - math.Pi.toFloat/2)) * canvasUnit, Point(0, 0), weaponLength * canvasUnit, 0, angle + move)


      ctx.fillStyle = "#ffffff"
      ctx.textAlign = "center"
      ctx.font = "normal normal 20px 楷体"
      ctx.fillText(s"${adventurer.name}", sx * canvasUnit, (sy + dy) * canvasUnit + 20)
      ctx.closePath()
    }
    adventurerMap.foreach{
      adventurer =>
        drawAnAdventurer(adventurer._2)
    }
  }

  def drawDead(offset: Point, canvasUnit: Int): Unit = {

    def drawADead(adventurer: Adventurer, step: Int): Unit = {

      val img = dom.document.createElement("img").asInstanceOf[html.Image]
      img.setAttribute("src", s"/thor/static/img/kill${7 - step}.png")

      val position = adventurer.getAdventurerState.position
      val r = config.getAdventurerRadiusByLevel(adventurer.level)

      val width = img.width
      val height = img.height

      ctx.drawImage(img, (position.x + offset.x) * canvasUnit - width/2, (position.y + offset.y) * canvasUnit - height/2)
    }

    dyingAdventurerMap.foreach{
      adventurer =>
        drawADead(adventurer._2._1, adventurer._2._2)
    }
  }

  def drawLevelUp(adventurer: Adventurer, step: Int, offSetTime: Long, offset:Point, canvasUnit: Int) = {
    if(adventurer.isUpdateLevel){
      val img = dom.document.createElement("img").asInstanceOf[html.Image]
      img.setAttribute("src", s"/thor/static/img/level-up.png")

      val width = img.width
      val height = img.height
      val position = adventurer.getAdventurerState.position
      val r = config.getAdventurerRadiusByLevel(adventurer.level)
      val moveDistance = getMoveDistance(adventurer, offSetTime)

      val movePerStep = 15
      val offsetHeight =  - 200 + movePerStep * (step - offSetTime.toFloat/config.frameDuration)
      val opacity = 0.1 * step

      println(step)

      ctx.save()
      ctx.globalAlpha = opacity
      ctx.drawImage(img, (position.x + offset.x + moveDistance.x) * canvasUnit - width/2, (position.y + offset.y + moveDistance.y - r) * canvasUnit - height/2 + offsetHeight)
      ctx.restore()
    }
  }


  def drawAdventurerByOffsetTime(offSetTime: Long, offset: Point, canvasUnit: Int): Unit ={

    drawAdventurer(offSetTime, offset, canvasUnit)
    drawDead(offset, canvasUnit)
    adventurerMap.get(myId).foreach{
      adventurer =>
        drawLevelUp(adventurer, adventurer.levelUpExecute, offSetTime, offset, canvasUnit)
    }
  }

}






