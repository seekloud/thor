package org.seekloud.thor.scene

//import javafx.scene.SnapshotParameters
//import javafx.scene.canvas.GraphicsContext
//import javafx.scene.paint.Color
import org.seekloud.thor.ThorSchemaBotImpl
import org.seekloud.thor.model.Constants._
import org.seekloud.thor.shared.ptcl.component.{Adventurer, Food, FoodState}
import org.seekloud.thor.shared.ptcl.model.Point
import org.seekloud.thor.utils.middleware.MiddleContextInFx
import org.seekloud.thor.utils.CanvasUtils

/**
  * User: Jason
  * Date: 2019/3/13
  * Time: 17:13
  */

class DrawLayerScene(impl: ThorSchemaBotImpl) {

  def window = Point(impl.canvasSize.x , impl.canvasSize.y)

  def drawGame4Bot(mainId: String, offSetTime: Long, canvasUnit: Float, canvasBounds: Point, mousePoint: List[Point]): Unit = {
    if (!impl.waitSyncData) {
      impl.adventurerMap.get(mainId) match {
        case Some(adventurer) =>
          //保持自己的adventurer在屏幕中央~
          val moveDistance = getMoveDistance(adventurer, offSetTime)
          val offset = canvasBounds / 2 - (adventurer.getAdventurerState.position + moveDistance)
          val a = System.currentTimeMillis()
          drawBorder.drawBackground(offset, canvasUnit, canvasBounds)
          val b = System.currentTimeMillis()
          if (b-a>5)
            println(s"draw background time span: ${b-a}")
          drawFood.drawFood(offset, canvasUnit, canvasBounds)
          val c = System.currentTimeMillis()
          if (c-b>5)
            println(s"draw food time span: ${c-b}")
          drawAdventure.drawAllPlayer(offSetTime, offset, canvasUnit, canvasBounds)
          drawAdventure.drawAll(offSetTime, offset, canvasUnit, canvasBounds)
          drawAdventure.drawSelf(mainId, offSetTime, offset, canvasUnit, canvasBounds)
          val d = System.currentTimeMillis()
          if (d-c>5)
            println(s"draw adventurer time span: ${d-c}")
          drawFood.drawBodyFood(offset, offSetTime, canvasUnit, canvasBounds)
          val e = System.currentTimeMillis()
          if (e-d>5)
            println(s"draw body food time span: ${e-d}")
          drawPosition.drawPosition(mainId)
          drawPlayerState.drawState(mainId)
//          drawMouse.drawMousePosition(mousePoint, offSetTime, offset, canvasUnit, canvasBounds)
//          drawEnergyBar(adventurer)
          val f = System.currentTimeMillis()
          if (f-e>5)
            println(s"draw bar time span: ${f-e}")

        case None => println("None!!!!!!")
      }
    }
    else {
      println("waitSyncData!!!!")
    }
  }

  def getMoveDistance(adventurer: Adventurer, offSetTime: Long): Point = {
    // 获取当前渲染帧与逻辑帧的偏移量
    val r = impl.config.getAdventurerRadiusByLevel(adventurer.level)
    val position = adventurer.getAdventurerState.position
    var moveDistance = Point(0, 0)

    if (adventurer.isMove && adventurer.isIntersect == 0) {
      moveDistance = impl.config.getMoveDistanceByFrame(adventurer.getAdventurerState.level, adventurer.isSpeedUp).rotate(adventurer.getAdventurerState.direction) * offSetTime.toFloat / impl.config.frameDuration
      //如果达到边界 则不再往外走
      if (position.x - r <= 0 || position.x + r >= impl.config.boundary.x) moveDistance = moveDistance.copy(x = 0)
      if (position.y - r <= 0 || position.y + r >= impl.config.boundary.y) moveDistance = moveDistance.copy(y = 0)
    }
    moveDistance
  }

  object drawFood {
    def drawFood(offset: Point, canvasUnit: Float, canvasBoundary: Point): Unit = {
      impl.ctx("food").clearRect(0, 0, layeredCanvasWidth, layeredCanvasHeight)
      impl.ctx("food").setFill("#000000")
      impl.ctx("food").fillRec(0, 0, layeredCanvasWidth, layeredCanvasHeight)
      impl.preFoodImage match {
        case Nil =>
          impl.foodMap.foreach { food =>
            drawAFood(food._2, offset, canvasUnit, canvasBoundary)
          }

        case _ => impl.foodMap.foreach { food =>
          drawFoodByPreImage(food._2, offset, canvasUnit, canvasBoundary)
        }
      }

    }

    def drawBodyFood(offset: Point, offsetTime: Long, canvasUnit: Float, canvasBoundary: Point): Unit = {
      def drawABodyFood(food: FoodState, curPosition: Point): Unit = {
        if (food.scatterStep.nonEmpty) {
          if (food.scatterStep.get > 0) {
            val moveDistance = (curPosition.moveTo(food.position, food.scatterStep.get) - curPosition) * offsetTime.toFloat / impl.config.frameDuration
            val newPosition = curPosition + moveDistance
            impl.preFoodImage match {
              case Nil => drawAFood(Food(food.copy(position = newPosition)), offset, canvasUnit, canvasBoundary)
              case _ => drawFoodByPreImage(Food(food.copy(position = newPosition)), offset, canvasUnit, canvasBoundary)
            }
          } else {
            impl.preFoodImage match {
              case Nil => drawAFood(Food(food), offset, canvasUnit, canvasBoundary)
              case _ => drawFoodByPreImage(Food(food), offset, canvasUnit, canvasBoundary)
            }
          }
        } else {
          impl.preFoodImage match {
            case Nil => drawAFood(Food(food), offset, canvasUnit, canvasBoundary)
            case _ => drawFoodByPreImage(Food(food), offset, canvasUnit, canvasBoundary)
          }
        }
      }

      impl.bodyFood.foreach { bf =>
        drawABodyFood(bf._2._1, bf._2._2)
      }
    }

    def drawAFood(food: Food, offset: Point, canvasUnit: Float, canvasBoundary: Point): Unit = {

      val img = impl.drawFrame.createImage(pictureMap(s"food-sheet0-${food.getFoodState.color}.png"))

      val r = impl.config.getRadiusByFoodLevel(food.getFoodState.level)
      val sx = food.getFoodState.position.x - r + offset.x
      val sy = food.getFoodState.position.y - r + offset.y
      val dx = 2 * r
      val dy = 2 * r

      if (0 < sx && sx < canvasBoundary.x && 0 < sy && sy < canvasBoundary.y) {
        //只绘制视角窗口内的食物
        impl.ctx("food").save()
        impl.ctx("food").drawImage(img, sx * canvasUnit, sy * canvasUnit, Some(dx * canvasUnit, dy * canvasUnit))
        impl.ctx("food").restore()
      }

    }

    def drawFoodByPreImage(food: Food, offset: Point, canvasUnit: Float, canvasBoundary: Point): Unit = {

      val color = food.getFoodState.color

      val r = impl.config.getRadiusByFoodLevel(food.getFoodState.level)
      val sx = food.getFoodState.position.x - r + offset.x
      val sy = food.getFoodState.position.y - r + offset.y
      val dx = 2 * r
      val dy = 2 * r

      if (0 < sx && sx < canvasBoundary.x && 0 < sy && sy < canvasBoundary.y) {
        //只绘制视角窗口内的食物
        impl.ctx("food").save()
        impl.ctx("food").drawImage(impl.preFoodImage(color), sx * canvasUnit, sy * canvasUnit, Some(dx * canvasUnit, dy * canvasUnit))
        impl.ctx("food").restore()
      }
    }
  }
 var a = 0
  object drawPosition {
    def drawPosition(mainId: String): Unit ={
      val windowScale = impl.canvasSize.x / (impl.config.boundary.x * impl.canvasUnit)
      def drawStar(adventurerMapX: Double, adventurerMapY: Double): Unit = {
        impl.ctx("position").save()
        impl.ctx("position").setFill("#FFFFFF")
        impl.ctx("position").fillRec(adventurerMapX - impl.canvasSize.x * windowScale / 2, adventurerMapY - impl.canvasSize.y * windowScale /2, impl.canvasSize.x * windowScale, impl.canvasSize.y *windowScale)
        impl.ctx("position").beginPath()
        impl.ctx("position").moveTo(adventurerMapX - 6.6, adventurerMapY - 2.0)
        impl.ctx("position").lineTo(adventurerMapX + 6.6, adventurerMapY - 2.0)
        impl.ctx("position").lineTo(adventurerMapX - 4.0, adventurerMapY + 6.0)
        impl.ctx("position").lineTo(adventurerMapX - 0.0, adventurerMapY - 7.0)
        impl.ctx("position").lineTo(adventurerMapX + 4.0, adventurerMapY + 6.0)
        impl.ctx("position").setFill("#b1cfed")
        impl.ctx("position").fill()
        impl.ctx("position").restore()
      }

      //获取比例
      a += 1
      val scale = window.x  / 600.0
      if (a % 100 == 0)
      println(s"window: $window, scale : $scale, windowScale: $windowScale")
      impl.ctx("position").save()
      impl.ctx("position").clearRect(0, 0, layeredCanvasWidth, layeredCanvasHeight)
      impl.ctx("position").setFill("#000000")
      impl.ctx("position").fillRec(0, 0, 400, 200)
      impl.adventurerMap.foreach{
        case adventurer if adventurer._1 == mainId =>
          val adventurerMapX = adventurer._2.position.x * scale
          val adventurerMapY = adventurer._2.position.y * scale
          drawStar(adventurerMapX, adventurerMapY)
        case _ => ()
      }
      impl.ctx("position").restore()
    }
  }

  object drawBorder {
    def drawBackground(offset: Point, canvasUnit: Float, canvasBoundary: Point): Unit = {
      impl.ctx("border").save()
      impl.ctx("border").clearRect(0, 0, layeredCanvasWidth, layeredCanvasHeight)
      impl.ctx("border").setFill("#000000")
      impl.ctx("border").fillRec(0, 0, 400, 200)
      impl.ctx("border").setFill("#171b1f")
      impl.ctx("border").fillRec(0, 0, canvasBoundary.x * canvasUnit, canvasBoundary.y * canvasUnit)
      impl.ctx("border").fill()
      val borderW = 10
      impl.ctx("border").setFill("#4A4B49")
      impl.ctx("border").rect(offset.x * canvasUnit, offset.y * canvasUnit, impl.config.boundary.x * canvasUnit, borderW)
      impl.ctx("border").rect(offset.x * canvasUnit, offset.y * canvasUnit, borderW, impl.config.boundary.y * canvasUnit)
      impl.ctx("border").rect((offset.x + impl.config.boundary.x) * canvasUnit - borderW, offset.y * canvasUnit, borderW, impl.config.boundary.y * canvasUnit)
      impl.ctx("border").rect(offset.x * canvasUnit, (offset.y + impl.config.boundary.y) * canvasUnit - borderW, impl.config.boundary.x * canvasUnit, borderW)
      impl.ctx("border").fill()
      impl.ctx("border").restore()
    }
  }

  object drawAdventure {

    def drawAnAdventurer(adventurer: Adventurer, offSetTime: Long, offset: Point, canvasUnit: Float, ctx: MiddleContextInFx): Unit = {
      val r = impl.config.getAdventurerRadiusByLevel(adventurer.level)
      val position = adventurer.getAdventurerState.position
      val moveDistance = getMoveDistance(adventurer, offSetTime)

      val sx = position.x + offset.x + moveDistance.x
      val sy = position.y + offset.y + moveDistance.y
      val dx = 2 * r
      val dy = 2 * r

      //画人物
      CanvasUtils.rotateImage("adventurer", impl.drawFrame, ctx, Nil, impl.preAdventurerImage, pictureMap(s"char${(adventurer.level % 21 - 1) / 4 + 1}-${(adventurer.level - 1) % 4}.png"), Point(sx, sy) * canvasUnit, Point(0, 0), dx * canvasUnit * 1.1.toFloat, 0, adventurer.getAdventurerState.direction, impl.preTime, adventurer.getAdventurerState.level)

      //出生保护
      impl.newbornAdventurerMap.get(adventurer.playerId) match {
        case Some(s) =>
          ctx.save()
          ctx.setFill("rgba(79,148,205,0.4)")
          ctx.setGlobalAlpha(0.8)
          ctx.beginPath()
          val flag = false
          ctx.arc(sx * canvasUnit, sy * canvasUnit, r * canvasUnit * 1.15, 0, 360, flag)
          ctx.closePath()
          ctx.fill()
          ctx.restore()
        case _ =>
      }

      //画武器
      var step: Float = 3
      var isAttacking = false
      impl.attackingAdventureMap.filterNot(_._2 < 0).get(adventurer.playerId) match {
        case Some(s) =>
          step = s
          if (step != 0) isAttacking = true
        case _ =>
      }
      val weaponLength = impl.config.getWeaponLengthByLevel(adventurer.getAdventurerState.level) * 1.2.toFloat
      val angle = adventurer.getAdventurerState.direction - math.Pi.toFloat * (3 * step + 1) / 10 //武器旋转角度
      val gap: Float = 0 // 武器离人物的距离
      val move: Float = if (isAttacking) math.Pi.toFloat * 3 / 10 * offSetTime.toFloat / impl.config.frameDuration else 0 //该渲染帧的角度偏移量，攻击中禁止移动
      val weaponPosition = Point(sx, sy) + Point(weaponLength / 2, r).rotate(angle + move)
      //        CanvasUtils.rotateImage("weapon", impl.drawFrame, ctx("allPlayer"), Nil, impl.preWeaponImage, pictureMap(s"weapon${(adventurer.level - 1) / 4 + 1}.png"), weaponPosition * canvasUnit, Point(0, 0), weaponLength * canvasUnit, 0, angle + move, impl.preTime, adventurer.getAdventurerState.level)
      //用户昵称
      ctx.save()
      ctx.setFill("#ffffff")
      ctx.setTextAlign("center")
      ctx.setFont("微软雅黑", 20)
      ctx.setTextBaseLine("top")
      ctx.fillText(s"${adventurer.name}", sx * canvasUnit, (sy + r) * canvasUnit + 20)
      ctx.restore()
    }

    def drawAllPlayer(offSetTime: Long, offset: Point, canvasUnit: Float, canvasBoundary: Point): Unit = {
      impl.ctx("allPlayer").clearRect(0, 0, layeredCanvasWidth, layeredCanvasHeight)
      impl.ctx("allPlayer").setFill("#000000")
      impl.ctx("allPlayer").fillRec(0, 0, 400, 200)
      impl.adventurerMap.foreach {
        adventurer =>
          if (!impl.dyingAdventurerMap.contains(adventurer._1)) {
            drawAnAdventurer(adventurer._2, offSetTime, offset, canvasUnit, impl.ctx("allPlayer"))
          }
      }
    }

    def drawAll(offSetTime: Long, offset: Point, canvasUnit: Float, canvasBoundary: Point): Unit = {
      impl.ctx("all").clearRect(0, 0, layeredCanvasWidth, layeredCanvasHeight)
      impl.ctx("all").setFill("#000000")
      impl.ctx("all").fillRec(0, 0, 400, 200)
      impl.adventurerMap.foreach {
        adventurer =>
          if (!impl.dyingAdventurerMap.contains(adventurer._1)) {
            drawAnAdventurer(adventurer._2, offSetTime, offset, canvasUnit, impl.ctx("all"))
          }
      }
    }

    def drawSelf(mainId: String, offSetTime: Long, offset: Point, canvasUnit: Float, canvasBoundary: Point): Unit = {
      impl.ctx("self").clearRect(0, 0, layeredCanvasWidth, layeredCanvasHeight)
      impl.ctx("self").setFill("#000000")
      impl.ctx("self").fillRec(0, 0, 400, 200)
      impl.adventurerMap.foreach {
        adventurer =>
          if (!impl.dyingAdventurerMap.contains(adventurer._1) && adventurer._1 == mainId) {
            drawAnAdventurer(adventurer._2, offSetTime, offset, canvasUnit, impl.ctx("self"))
          }
      }
    }


  }

  object drawMouse {
    def drawMouse(p: Point): Unit = {
      impl.ctx("mouse").save()
      impl.ctx("mouse").clearRect(0, 0, layeredCanvasWidth, layeredCanvasHeight)
      impl.ctx("mouse").setFill("#000000")
      impl.ctx("mouse").fillRec(0, 0, 400, 200)
      impl.ctx("mouse").beginPath()
      impl.ctx("mouse").moveTo(p.x - 6.6, p.y - 2.0)
      impl.ctx("mouse").lineTo(p.x + 6.6, p.y - 2.0)
      impl.ctx("mouse").lineTo(p.x - 4.0, p.y + 6.0)
      impl.ctx("mouse").lineTo(p.x - 0.0, p.y - 7.0)
      impl.ctx("mouse").lineTo(p.x + 4.0, p.y + 6.0)
      impl.ctx("mouse").setFill("#b1cfed")
      impl.ctx("mouse").fill()
      impl.ctx("mouse").restore()
    }

    def drawMousePosition(position: List[Point], offSetTime: Long, offset: Point, canvasUnit: Float, canvasBoundary: Point): Unit = {
      position.headOption.foreach { p =>
        val offset = position.reverse.head - p
        drawMouse(p + offset * offSetTime / impl.config.frameDuration)
      }
    }
  }

  object drawPlayerState {
    def drawState(maidId: String): Unit = {
      impl.adventurerMap.foreach{ adventurer =>
        if (!impl.dyingAdventurerMap.contains(adventurer._1) && adventurer._1 == maidId) {
          drawAState(adventurer._2)
        }
      }
    }

    def drawAState(adventurer: Adventurer): Unit = {
      println(adventurer.radius, adventurer.faceDirection)
    }
  }


}
