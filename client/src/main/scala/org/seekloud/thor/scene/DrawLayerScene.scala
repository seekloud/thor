package org.seekloud.thor.scene

//import javafx.scene.SnapshotParameters
//import javafx.scene.canvas.GraphicsContext
//import javafx.scene.paint.Color
import java.nio.ByteBuffer

import com.google.protobuf.ByteString
import javafx.scene.SnapshotParameters
import javafx.scene.image.{Image, WritableImage}
import javafx.scene.paint.Color
import org.seekloud.esheepapi.pb.observations.{ImgData, LayeredObservation}
import org.seekloud.thor.game.ThorSchemaBotImpl
import org.seekloud.thor.model.Constants._
import org.seekloud.thor.shared.ptcl.component.{Adventurer, Food, FoodState}
import org.seekloud.thor.shared.ptcl.model.{Point, Score}
import org.seekloud.thor.shared.ptcl.protocol.ThorGame.UserActionEvent
import org.seekloud.thor.utils.middleware.MiddleContextInFx
import org.seekloud.thor.utils.CanvasUtils


/**
  * User: Jason
  * Date: 2019/3/13
  * Time: 17:13
  */

class DrawLayerScene(impl: ThorSchemaBotImpl) {

  def window = Point(impl.canvasSize.x , impl.canvasSize.y)
  def window4Human: Point = Point(impl.canvasSize.x - 6 , impl.canvasSize.y - 6) * 4
  private val barLength = 400
  private val barHeight = 20
  private val r = barHeight / 2

  private def barLeft = (impl.canvasSize.x * 4 - barLength) / 2

  private def barTop = impl.canvasSize.y * 4 - barHeight - 50

  val mapImg = new Image("img/background.png")
  val hammerImg = new Image("img/hammer.png")
  val starImg = new Image("img/star.png")
  val crownImg = new Image("img/crown.png")

  def drawGame4Bot(mainId: String, offSetTime: Long, canvasUnit: Float, canvasUnit4Huge: Float,
    canvasBounds: Point, mousePoint: List[Point], actions: Map[Int, List[UserActionEvent]]): Unit = {
    if (!impl.waitSyncData) {
      impl.adventurerMap.get(mainId) match {
        case Some(adventurer) =>
          //保持自己的adventurer在屏幕中央~
          val moveDistance = getMoveDistance(adventurer, offSetTime)
          val offset = canvasBounds / 2 - (adventurer.getAdventurerState.position + moveDistance)
//          drawBorder.drawBackground4Human(offset, canvasUnit4Huge, canvasBounds)
//          drawFood.drawFood4Human(offset, canvasUnit4Huge, canvasBounds)
//          drawAdventure.drawAll4Human(mainId, offSetTime, offset, canvasUnit4Huge, canvasBounds, actions)
//          drawHumanView.drawEnergyBar(impl.adventurerMap(mainId))
//          drawHumanView.drawSmallMap(mainId)

          drawBorder.drawBackground(offset, canvasUnit, canvasBounds)
          drawFood.drawFood(offset, canvasUnit, canvasBounds)
          drawFood.drawBodyFood(offset, offSetTime, canvasUnit, canvasBounds)
          drawAdventure.drawAllPlayer(mainId, offSetTime, offset, canvasUnit, canvasBounds)
          drawAdventure.drawAll(mainId, offSetTime, offset, canvasUnit, canvasBounds)
          drawAdventure.drawSelf(mainId, offSetTime, offset, canvasUnit, canvasBounds)
          drawPosition.drawPosition(mainId)
          drawPlayerState.drawState(mainId)
//          drawMouse.drawMousePosition(mousePoint, offSetTime, offset, canvasUnit, canvasBounds)

        case None => println("None!!!!!!")
      }
    }
    else {
      println("waitSyncData!!!!")
    }
  }

  def drawGame4Human(mainId: String, offSetTime: Long, canvasUnit: Float, canvasUnit4Huge: Float,
    canvasBounds: Point, mousePoint: List[Point], actions: Map[Int, List[UserActionEvent]]): Unit = {
    if (!impl.waitSyncData) {
      impl.adventurerMap.get(mainId) match {
        case Some(adventurer) =>
          //保持自己的adventurer在屏幕中央~
          val moveDistance = getMoveDistance(adventurer, offSetTime)
          val offset = canvasBounds / 2 - (adventurer.getAdventurerState.position + moveDistance)
          drawBorder.drawBackground4Human(offset, canvasUnit4Huge, canvasBounds)
          drawFood.drawFood4Human(offset, canvasUnit4Huge, canvasBounds)
          drawAdventure.drawAll4Human(mainId, offSetTime, offset, canvasUnit4Huge, canvasBounds, actions)
          drawHumanView.drawEnergyBar(impl.adventurerMap(mainId))
          drawHumanView.drawSmallMap(mainId)
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
            drawAFood(food._2, offset, canvasUnit, canvasBoundary, impl.ctx("food"))
          }

        case _ => impl.foodMap.foreach { food =>
          drawFoodByPreImage(food._2, offset, canvasUnit, canvasBoundary, impl.ctx("food"))
        }
      }
    }

    def drawFood4Human(offset: Point, canvasUnit: Float, canvasBoundary: Point): Unit = {
      impl.preFoodImage match {
        case Nil =>
          impl.foodMap.foreach { food =>
            drawAFood(food._2, offset, canvasUnit, canvasBoundary, impl.ctx("human"))
          }

        case _ => impl.foodMap.foreach { food =>
          drawFoodByPreImage(food._2, offset, canvasUnit, canvasBoundary, impl.ctx("human"))
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
              case Nil => 
                drawAFood(Food(food.copy(position = newPosition)), offset, canvasUnit, canvasBoundary,impl.ctx("food"))
                drawAFood(Food(food.copy(position = newPosition)), offset, canvasUnit * 4, canvasBoundary,impl.ctx("human"))
              case _ => 
                drawFoodByPreImage(Food(food.copy(position = newPosition)), offset, canvasUnit, canvasBoundary,impl.ctx("food"))
                drawFoodByPreImage(Food(food.copy(position = newPosition)), offset, canvasUnit * 4, canvasBoundary,impl.ctx("human"))
            }
          } else {
            impl.preFoodImage match {
              case Nil => 
                drawAFood(Food(food), offset, canvasUnit, canvasBoundary, impl.ctx("food"))
                drawAFood(Food(food), offset, canvasUnit * 4, canvasBoundary, impl.ctx("human"))
              case _ => 
                drawFoodByPreImage(Food(food), offset, canvasUnit, canvasBoundary, impl.ctx("food"))
                drawFoodByPreImage(Food(food), offset, canvasUnit * 4, canvasBoundary, impl.ctx("human"))
            }
          }
        } else {
          impl.preFoodImage match {
            case Nil =>
              drawAFood(Food(food), offset, canvasUnit, canvasBoundary, impl.ctx("food"))
              drawAFood(Food(food), offset, canvasUnit * 4, canvasBoundary, impl.ctx("human"))
            case _ =>
              drawFoodByPreImage(Food(food), offset, canvasUnit, canvasBoundary, impl.ctx("food"))
              drawFoodByPreImage(Food(food), offset, canvasUnit * 4, canvasBoundary, impl.ctx("human"))
          }
        }
      }

      impl.bodyFood.foreach { bf =>
        drawABodyFood(bf._2._1, bf._2._2)
      }
    }

    def drawAFood(food: Food, offset: Point, canvasUnit: Float, canvasBoundary: Point, ctx: MiddleContextInFx): Unit = {

      val img = impl.drawFrame.createImage(pictureMap(s"food-sheet0-${food.getFoodState.color}.png"))

      val r = impl.config.getRadiusByFoodLevel(food.getFoodState.level)
      val sx = food.getFoodState.position.x - r + offset.x
      val sy = food.getFoodState.position.y - r + offset.y
      val dx = 2 * r
      val dy = 2 * r

      if (0 < sx && sx < canvasBoundary.x && 0 < sy && sy < canvasBoundary.y) {
        //只绘制视角窗口内的食物
        ctx.save()
        ctx.drawImage(img, sx * canvasUnit, sy * canvasUnit, Some(dx * canvasUnit, dy * canvasUnit))
        ctx.restore()
      }

    }

    def drawFoodByPreImage(food: Food, offset: Point, canvasUnit: Float, canvasBoundary: Point, ctx: MiddleContextInFx): Unit = {

      val color = food.getFoodState.color

      val r = impl.config.getRadiusByFoodLevel(food.getFoodState.level)
      val sx = food.getFoodState.position.x - r + offset.x
      val sy = food.getFoodState.position.y - r + offset.y
      val dx = 2 * r
      val dy = 2 * r

      if (0 < sx && sx < canvasBoundary.x && 0 < sy && sy < canvasBoundary.y) {
        //只绘制视角窗口内的食物
        ctx.save()
        ctx.drawImage(impl.preFoodImage(color), sx * canvasUnit, sy * canvasUnit, Some(dx * canvasUnit, dy * canvasUnit))
        ctx.restore()
      }
    }
  }

  object drawPosition {
    def drawPosition(mainId: String): Unit ={
      val windowScale = impl.canvasSize.x / (impl.config.boundary.x * impl.canvasUnit)
      def drawStar(adventurerMapX: Double, adventurerMapY: Double): Unit = {
        impl.ctx("position").save()
        impl.ctx("position").setFill("#FFFFFF")
        impl.ctx("position").fillRec(adventurerMapX - impl.canvasSize.x * windowScale / 2, adventurerMapY - impl.canvasSize.y * windowScale /2, impl.canvasSize.x * windowScale, impl.canvasSize.y *windowScale)
//        impl.ctx("position").beginPath()
//        impl.ctx("position").moveTo(adventurerMapX - 6.6, adventurerMapY - 2.0)
//        impl.ctx("position").lineTo(adventurerMapX + 6.6, adventurerMapY - 2.0)
//        impl.ctx("position").lineTo(adventurerMapX - 4.0, adventurerMapY + 6.0)
//        impl.ctx("position").lineTo(adventurerMapX - 0.0, adventurerMapY - 7.0)
//        impl.ctx("position").lineTo(adventurerMapX + 4.0, adventurerMapY + 6.0)
//        impl.ctx("position").setFill("#b1cfed")
//        impl.ctx("position").fill()
        impl.ctx("position").restore()
      }

      //获取比例
      val scale = window.x  / 600.0
      impl.ctx("position").save()
      impl.ctx("position").clearRect(0, 0, layeredCanvasWidth, layeredCanvasHeight)
      impl.ctx("position").setFill("#000000")
      impl.ctx("position").fillRec(0, 0, layeredCanvasWidth, layeredCanvasHeight)
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
      impl.ctx("border").fillRec(0, 0, layeredCanvasWidth, layeredCanvasHeight)
      val borderW = 10
      impl.ctx("border").setFill("#FFFFFF")
      impl.ctx("border").fillRec(offset.x * canvasUnit, offset.y * canvasUnit, impl.config.boundary.x * canvasUnit, borderW)
      impl.ctx("border").fillRec(offset.x * canvasUnit, offset.y * canvasUnit, borderW, impl.config.boundary.y * canvasUnit)
      impl.ctx("border").fillRec((offset.x + impl.config.boundary.x) * canvasUnit - borderW, offset.y * canvasUnit, borderW, impl.config.boundary.y * canvasUnit)
      impl.ctx("border").fillRec(offset.x * canvasUnit, (offset.y + impl.config.boundary.y) * canvasUnit - borderW, impl.config.boundary.x * canvasUnit, borderW)
      impl.ctx("border").restore()
    }

    def drawBackground4Human(offset: Point, canvasUnit: Float, canvasBoundary: Point): Unit = {
      impl.ctx("human").save()
      impl.ctx("human").clearRect(0, 0, CanvasWidth, CanvasHeight)
      impl.ctx("human").setFill("#171b1f")
      impl.ctx("human").fillRec(0, 0, canvasBoundary.x * canvasUnit * 1.5, canvasBoundary.y * canvasUnit * 1.5)
      impl.ctx("human").fill()
      impl.ctx("human").drawImage(mapImg, offset.x * canvasUnit, offset.y * canvasUnit, Some(impl.config.boundary.x / 2 * canvasUnit, impl.config.boundary.y / 2 * canvasUnit))
      impl.ctx("human").drawImage(mapImg, (offset.x + impl.config.boundary.x / 2) * canvasUnit, offset.y * canvasUnit, Some(impl.config.boundary.x / 2 * canvasUnit, impl.config.boundary.y / 2 * canvasUnit))
      impl.ctx("human").drawImage(mapImg, offset.x * canvasUnit, (offset.y + impl.config.boundary.y / 2) * canvasUnit, Some(impl.config.boundary.x / 2 * canvasUnit, impl.config.boundary.y / 2 * canvasUnit))
      impl.ctx("human").drawImage(mapImg, (offset.x + impl.config.boundary.x / 2) * canvasUnit, (offset.y + impl.config.boundary.y / 2) * canvasUnit, Some(impl.config.boundary.x / 2 * canvasUnit, impl.config.boundary.y / 2 * canvasUnit))
      val borderW = 10
      impl.ctx("human").setFill("#4A4B49")
      impl.ctx("human").rect(offset.x * canvasUnit, offset.y * canvasUnit, impl.config.boundary.x * canvasUnit, borderW)
      impl.ctx("human").rect(offset.x * canvasUnit, offset.y * canvasUnit, borderW, impl.config.boundary.y * canvasUnit)
      impl.ctx("human").rect((offset.x + impl.config.boundary.x) * canvasUnit - borderW, offset.y * canvasUnit, borderW, impl.config.boundary.y * canvasUnit)
      impl.ctx("human").rect(offset.x * canvasUnit, (offset.y + impl.config.boundary.y) * canvasUnit - borderW, impl.config.boundary.x * canvasUnit, borderW)

      impl.ctx("human").fill()
      impl.ctx("human").restore()
    }
  }

  object drawAdventure {

    def drawAnAdventurer(adventurer: Adventurer, offSetTime: Long, offset: Point, canvasUnit: Float, ctx: MiddleContextInFx, color: String, isHuman: Boolean = false): Unit = {
      val r = impl.config.getAdventurerRadiusByLevel(adventurer.level)
      val position = adventurer.getAdventurerState.position
      val moveDistance = getMoveDistance(adventurer, offSetTime)

      val sx = position.x + offset.x + moveDistance.x
      val sy = position.y + offset.y + moveDistance.y
      val dx = 2 * r
      val dy = 2 * r

      if (adventurer.isSpeedUp) { //加速特效
        val height = impl.config.getAdventurerRadiusByLevel(adventurer.level) * 2 * canvasUnit
        val width = 3 * height

        CanvasUtils.rotateImage("speed", impl.drawFrame, ctx, Nil, Nil, "img/speedparticles.png", Point(sx, sy) * canvasUnit, Point(-height, 0), width, height, adventurer.getAdventurerState.direction, impl.preTime, adventurer.getAdventurerState.level)
      }

      //画人物
      if (isHuman)
        CanvasUtils.rotateImage("adventurer", impl.drawFrame, ctx, Nil, impl.preAdventurerImage, pictureMap(s"char${(adventurer.level % 21 - 1) / 4 + 1}-${(adventurer.level - 1) % 4}.png"), Point(sx, sy) * canvasUnit, Point(0, 0), dx * canvasUnit * 1.1.toFloat, 0, adventurer.getAdventurerState.direction, impl.preTime, adventurer.getAdventurerState.level)
      else
        CanvasUtils.rotateColor("adventurer", impl.drawFrame, ctx, impl.preAdventurerImage, pictureMap(s"char${(adventurer.level % 21 - 1) / 4 + 1}-${(adventurer.level - 1) % 4}.png"),
        Point(sx, sy) * canvasUnit, Point(0, 0), dx * canvasUnit * 1.1.toFloat, 0, adventurer.getAdventurerState.direction, adventurer.getAdventurerState.level, color, adventurer.radius, canvasUnit)

      //出生保护
      impl.newbornAdventurerMap.get(adventurer.playerId) match {
        case Some(s) =>
          ctx.save()
          ctx.setFill("rgba(79,148,205,0.4)")
          ctx.setGlobalAlpha(0.8)
          ctx.beginPath()
          ctx.arc(sx * canvasUnit, sy * canvasUnit, r * canvasUnit * 1.3, 0, 360, counterclockwise = false)
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
      if (isHuman) CanvasUtils.rotateImage("weapon", impl.drawFrame, ctx, Nil, impl.preWeaponImage,
        pictureMap(s"weapon${(adventurer.level - 1) / 4 + 1}.png"), weaponPosition * canvasUnit, Point(0, 0), weaponLength * canvasUnit, 0, angle + move, impl.preTime, adventurer.getAdventurerState.level)
      //用户昵称

      if (isHuman) {
        ctx.save()
        ctx.setFill("#ffffff")
        ctx.setTextAlign("center")
        ctx.setFont("微软雅黑", 20)
        ctx.setTextBaseLine("top")
        ctx.fillText(s"${adventurer.name}", sx * canvasUnit, (sy + r) * canvasUnit + 20)
        ctx.restore()

        ctx.save()
        if (adventurer.stickKillNum > 0) {
          for (i <- 1 to adventurer.stickKillNum)
            ctx.drawImage(starImg, (sx + i * 2) * canvasUnit - 35, (sy + r) * canvasUnit + 45, Some(20, 20))
        }
        ctx.restore()
      }
      
    }

    def drawAllPlayer(mainId: String, offSetTime: Long, offset: Point, canvasUnit: Float,
      canvasBoundary: Point): Unit = {
      impl.ctx("allPlayer").clearRect(0, 0, layeredCanvasWidth * 2, layeredCanvasHeight * 2 + 210)
      impl.ctx("allPlayer").setFill("#000000")
      impl.ctx("allPlayer").fillRec(0, 0, 400 * 2, 200 * 2 )
      impl.adventurerMap.foreach {
        adventurer =>
          if (!impl.dyingAdventurerMap.contains(adventurer._1)) {
            if (adventurer._1 == mainId)
              drawAnAdventurer(adventurer._2, offSetTime, offset, canvasUnit, impl.ctx("allPlayer"), selfColor)
            else
              drawAnAdventurer(adventurer._2, offSetTime, offset, canvasUnit, impl.ctx("allPlayer"), othersColor)
          }
      }
    }

    def drawAll(mainId: String, offSetTime: Long, offset: Point, canvasUnit: Float, canvasBoundary: Point): Unit = {
      impl.ctx("all").clearRect(0, 0, layeredCanvasWidth, layeredCanvasHeight)
      impl.ctx("all").setFill("#000000")
      impl.ctx("all").fillRec(0, 0, layeredCanvasWidth, layeredCanvasHeight)
      impl.adventurerMap.foreach {
        adventurer =>
          if (!impl.dyingAdventurerMap.contains(adventurer._1)) {
            if (adventurer._1 == mainId)
              drawAnAdventurer(adventurer._2, offSetTime, offset, canvasUnit, impl.ctx("all"), selfColor)
            else
              drawAnAdventurer(adventurer._2, offSetTime, offset, canvasUnit, impl.ctx("all"), othersColor)
          }
      }
    }

    def drawSelf(mainId: String, offSetTime: Long, offset: Point, canvasUnit: Float, canvasBoundary: Point): Unit = {
      impl.ctx("self").clearRect(0, 0, layeredCanvasWidth, layeredCanvasHeight)
      impl.ctx("self").setFill("#000000")
      impl.ctx("self").fillRec(0, 0, layeredCanvasWidth, layeredCanvasHeight)
      impl.adventurerMap.foreach {
        adventurer =>
          if (!impl.dyingAdventurerMap.contains(adventurer._1) && adventurer._1 == mainId) {
            drawAnAdventurer(adventurer._2, offSetTime, offset, canvasUnit, impl.ctx("self"), selfColor)
          }
      }
    }

    def drawAll4Human(mainId: String, offSetTime: Long, offset: Point, canvasUnit: Float, 
      canvasBoundary: Point, myActions: Map[Int, List[UserActionEvent]]): Unit = {
      impl.adventurerMap.foreach {
        adventurer =>
          if (!impl.dyingAdventurerMap.contains(adventurer._1)) {
            drawAnAdventurer(adventurer._2, offSetTime, offset, canvasUnit, impl.ctx("human"), selfColor, isHuman = true)
          }
      }
      impl.ctx("human").setFill("#000000")
      impl.ctx("human").fillRec(0, 405, 400 * 2, 210 )
      impl.ctx("human").setFill("#FFFFFF")
      impl.ctx("human").fillRec(0, 400, 400 * 2, 5 )
      val baseLine4A = 2
      var index4A = 0
      val textLineHeight = 15
      var size = 0
      var actions = Map.empty[Int, String]
      myActions.foreach{ a =>
        a._2.foreach{ b =>
          actions += (a._1 -> b.toString)
        }
      }
      myActions.foreach( a => size += a._2.size)
      if(actions.size >= 12){
        actions.toList.sortBy(_._1).takeRight(12).foreach{ a =>
          impl.ctx("human").fillText(s"frame:${a._1},action:${a._2}",10,400 + (index4A + baseLine4A) * textLineHeight)
          index4A += 1
        }
      }
      else {
        actions.toList.sortBy(_._1).foreach{ a =>
          impl.ctx("human").fillText(s"frame:${a._1},action:${a._2}",10,400 + (index4A + baseLine4A) * textLineHeight)
          index4A += 1
        }
      }
    }
    
  }

  object drawMouse {
    def drawMouse(p: Point): Unit = {
      impl.ctx("mouse").save()
      impl.ctx("mouse").clearRect(0, 0, layeredCanvasWidth, layeredCanvasHeight)
      impl.ctx("mouse").setFill("#000000")
      impl.ctx("mouse").fillRec(0, 0, layeredCanvasWidth, layeredCanvasHeight)
      impl.ctx("mouse").setFill("#FFFFFF")
      impl.ctx("mouse").beginPath()
      impl.ctx("mouse").arc(p.x, p.y, impl.canvasUnit * 2, 0, 360, counterclockwise = false)
      impl.ctx("mouse").closePath()
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
    private val speedUpRate: Float = impl.config.getThorGameConfigImpl().adventurerParams.speedUpRate
    private val maxSpeed: Float = impl.config.getThorGameConfigImpl().adventurerParams.speeds.speeds.max
    private val maxRadius: Float = impl.config.getThorGameConfigImpl().adventurerParams.radius.max
    private val speedUnit = 150 / (maxSpeed * speedUpRate)
    private val radiusUnit = 150 / maxRadius

    def drawState(maidId: String): Unit = {
      impl.adventurerMap.foreach{ adventurer =>
        if (!impl.dyingAdventurerMap.contains(adventurer._1) && adventurer._1 == maidId) {
          drawAState(adventurer._2)
        }
      }
    }

    def drawAState(adventurer: Adventurer): Unit = {
      val isSpeedUp = adventurer.isSpeedUp
      val radius = adventurer.radius
      val speedByLevel = impl.config.getThorSpeedByLevel(adventurer.level).x
      val speed = if (isSpeedUp) speedUpRate * speedByLevel else speedByLevel
      val maxEnergy = impl.config.getThorGameConfigImpl().getMaxEnergyByLevel(adventurer.level)
      val energy = adventurer.energy
      val energyUnit = 150 / maxEnergy
      val isNewBorn = impl.newbornAdventurerMap.get(adventurer.playerId) match {
        case Some(_) => 1
        case _ => 0
      }
      val leftRemained = 25
      val w = 20
      impl.ctx("state").save()
      impl.ctx("state").clearRect(0, 0, layeredCanvasWidth, layeredCanvasHeight)
      impl.ctx("state").setFill("#000000")
      impl.ctx("state").fillRec(0, 0, layeredCanvasWidth, layeredCanvasHeight)
      impl.ctx("state").setFill("#FFFFFF")
      impl.ctx("state").fillRec(leftRemained , 4, radius * radiusUnit, w)
      impl.ctx("state").setFill("#FF0000")
      impl.ctx("state").fillRec(leftRemained , 28, speed * speedUnit, w)
      impl.ctx("state").setFill("#00FF00")
      impl.ctx("state").fillRec(leftRemained , 52, energy * energyUnit, w)
      impl.ctx("state").setFill("#0000FF")
      impl.ctx("state").fillRec(leftRemained , 76, isNewBorn * 150, w)
      impl.ctx("state").restore()
    }
  }
  
  object drawHumanView {
    def drawArcRect(r: Double, width: Double, height: Double, left: Double, top: Double): Unit = {
      if (width >= 2 * r) {
        impl.ctx("human").save()
        impl.ctx("human").beginPath()
        impl.ctx("human").arc(left + r, top + r, r, 90, 270, counterclockwise = false)
        impl.ctx("human").lineTo(left + width - r, top + height)
        impl.ctx("human").arc(left + width - r, top + r, r, 270, 450, counterclockwise = false)
        impl.ctx("human").lineTo(left + r, top)
        impl.ctx("human").closePath()
        impl.ctx("human").fill()
        impl.ctx("human").restore()
      }
    }

    def drawEnergyBar(adventurer: Adventurer): Unit = {
      impl.ctx("human").save()

      impl.ctx("human").setFill("#FFFFFF")
      drawArcRect(r, barLength, barHeight, barLeft, barTop)

      impl.ctx("human").setFill("#FF0000")
      val preLevel = if (adventurer.level == 1) 0 else impl.config.getMaxEnergyByLevel((adventurer.level - 1).toByte)
      val nowLevel = impl.config.getMaxEnergyByLevel(adventurer.level)
      val fillLength = math.min((adventurer.energy - preLevel).toFloat / (nowLevel - preLevel) * barLength, barLength)
      drawArcRect(r - 1, fillLength - 2, barHeight - 2, barLeft + 1, barTop + 1)

      impl.ctx("human").setFill("#FFFFFF")
      impl.ctx("human").setTextAlign("center")
      impl.ctx("human").setFont("Comic Sans Ms", 36)
      impl.ctx("human").setTextBaseLine("top")
      impl.ctx("human").fillText(adventurer.level.toString, barLeft - 32, barTop - 15)
      impl.ctx("human").restore()
    }

    def baseFont: Float = window4Human.x / 1440

    def drawTextLine(str: String, x: Double, lineNum: Int, lineBegin: Int = 0 , tp:Int): Unit = {
      impl.ctx("human").save()
      impl.ctx("human").setTextBaseLine("top")
      if (tp == 1)
        impl.ctx("human").setFont("Comic Sans MS", baseFont * 20)
      else if (tp == 2){
        impl.ctx("human").setFont("Comic Sans MS", baseFont * 16)
        impl.ctx("human").setFill("#fdffff")}
      else{
        impl.ctx("human").setFont("Comic Sans MS", baseFont * 16)
        impl.ctx("human").setFill("#ffff00")
      }

      impl.ctx("human").fillText(str, x, (lineNum + lineBegin) * window4Human.x * 0.01)
      impl.ctx("human").restore()
    }
    
    def drawRank(Rank: List[Score], CurrentOrNot: Boolean, shortId: Byte): Unit = {
      val text = "———————排行榜———————"
      val RankBaseLine = 3
      var index = 0
      var yourRank = 100
      var yourNameIn = false
      val begin = if (CurrentOrNot) 10 else window4Human.x * 0.78
      var last = ""
      impl.ctx("human").save()
      impl.ctx("human").setFill("rgba(0,0,0,0.6)")
      impl.ctx("human").fillRec(begin, 0, window4Human.x * 0.25, window4Human.x * 0.25)
      impl.ctx("human").setFill("#fdffff")
      impl.ctx("human").setTextAlign("start")
      drawTextLine(s"   $text", 150 + window4Human.x * 0.01, 0, 2, 1)

      Rank.foreach { score =>
        index += 1
        if (score.bId == shortId) yourRank = index
        if (impl.playerIdMap.exists(_._1 == score.bId)) {
          val fullName = impl.playerIdMap(score.bId)._2
          val name = if (fullName.length <= 4) fullName.take(4) else fullName.take(4) + "..."
          if (index < 10) {
            impl.ctx("human").setTextAlign("left")
            if (score.bId == shortId) {
              yourNameIn = true
              drawTextLine(s"【$index】  $name ", begin + window4Human.x * 0.01, index * 2, RankBaseLine, 3)
              drawTextLine(s" 分数: ${score.e}", begin + window4Human.x * 0.11, index * 2, RankBaseLine, 3)
              drawTextLine(s" 击杀数: ${score.k}", begin + window4Human.x * 0.18, index * 2, RankBaseLine, 3)
            }
            else {
              drawTextLine(s"【$index】  $name ", begin + window4Human.x * 0.01, index * 2, RankBaseLine, 2)
              drawTextLine(s" 分数: ${score.e}", begin + window4Human.x * 0.11, index * 2, RankBaseLine, 2)
              drawTextLine(s" 击杀数: ${score.k}", begin + window4Human.x * 0.18, index * 2, RankBaseLine, 2)
            }

          }
        }
      }
      index += 1
      if (!yourNameIn) {
        Rank.find(_.bId == shortId) match {
          case Some(yourScore) =>
            if (impl.playerIdMap.exists(_._1 == yourScore.bId)) {
              val fullName = impl.playerIdMap(yourScore.bId)._2
              val name = if (fullName.length <= 4) fullName.take(4) + "   " else fullName.take(4) + "..."
              drawTextLine(s"【$yourRank】  $name ", begin + window4Human.x * 0.01, 20, RankBaseLine, 3)
              drawTextLine(s" 分数: ${yourScore.e}", begin + window4Human.x * 0.11, 20, RankBaseLine, 3)
              drawTextLine(s" 击杀数: ${yourScore.k}", begin + window4Human.x * 0.18, 20, RankBaseLine, 3)
            }
          case None =>
        }
      }
      impl.ctx("human").restore()
    }

    def drawSmallMap(mainId: String): Unit ={

      def drawStar(adventurerMapX: Double, adventurerMapY: Double): Unit = {
        impl.ctx("human").save()
        impl.ctx("human").beginPath()
        impl.ctx("human").moveTo(adventurerMapX - 6.6, adventurerMapY - 2.0)
        impl.ctx("human").lineTo(adventurerMapX + 6.6, adventurerMapY - 2.0)
        impl.ctx("human").lineTo(adventurerMapX - 4.0, adventurerMapY + 6.0)
        impl.ctx("human").lineTo(adventurerMapX - 0.0, adventurerMapY - 7.0)
        impl.ctx("human").lineTo(adventurerMapX + 4.0, adventurerMapY + 6.0)
        impl.ctx("human").setFill("#b1cfed")
        impl.ctx("human").fill()
        impl.ctx("human").restore()
      }

      def drawCrown(adventurerMapX: Double, adventurerMapY: Double): Unit = {
        impl.ctx("human").save()
        impl.ctx("human").beginPath()
        impl.ctx("human").drawImage(crownImg, adventurerMapX - 6, adventurerMapY - 6, Some(12,12))
        impl.ctx("human").restore()
      }


      //获取比例
      val scale = (window4Human.x * 0.2) / 600.0
      impl.ctx("human").save()
      impl.ctx("human").setFill("rgba(0,0,0,0.4)")
      impl.ctx("human").fillRec(10, window4Human.y - window4Human.x * 0.11, window4Human.x * 0.2, window4Human.x * 0.1)
      impl.adventurerMap.foreach{
        case adventurer if adventurer._1 == mainId =>
          val adventurerMapX = 10 + adventurer._2.position.x * scale
          val adventurerMapY = window4Human.y - window4Human.x * 0.11 + adventurer._2.position.y * scale
          drawStar(adventurerMapX, adventurerMapY)
        case adventurer if adventurer._2.level >= 21 =>
          val adventurerMapX = 10 + adventurer._2.position.x * scale
          val adventurerMapY =  window4Human.y - window4Human.x * 0.11 + adventurer._2.position.y * scale
          drawCrown(adventurerMapX, adventurerMapY)
        case _ => ()
      }
      impl.ctx("human").restore()
    }
    
  }

  def drawGameLoading(ctx: MiddleContextInFx): Unit = {
    val h = ctx.getCanvas.getHeight
    val w = ctx.getCanvas.getWidth
    ctx.setFill("#000000")
    ctx.fillRec(0, 0, w, h)
    ctx.setFill("rgb(250, 250, 250)")
    ctx.setTextAlign("center")
    ctx.setFont("Helvetica", 20)
    val text = "Die Die Die"
    val l = ctx.measureText(text)
    ctx.fillText(text, (w - l) / 2, h / 3)
  }

  import org.seekloud.thor.common.BotSettings.isGray

  def getHumanImageData: ImgData = {
    val humanCtx = impl.ctx.find(_._1 == "human").get
    getImageData(humanCtx)._2
  }

  def getLayerImageData: LayeredObservation ={
    val ctxList = impl.ctx
    val a = ctxList.map(c => getImageData(c))
    val layeredObservation: LayeredObservation = LayeredObservation(
      a.get("position"),
      a.get("border"),
      a.get("food"),
      a.get("allPlayer"),
      a.get("all"),
      a.get("self"),
      a.get("mouse"),
      a.get("state")
    )
    layeredObservation
  }

  def getImageData(ctx: (String, MiddleContextInFx)): (String, ImgData) = {
    val canvas = ctx._2.getCanvas
    var height = canvas.getHeight.toInt
    if (ctx._1 == "human") height -= 210
    val width = canvas.getWidth.toInt
    val params = new SnapshotParameters
    val id = ctx._1
    params.setFill(Color.TRANSPARENT)

    val writableImage = canvas.snapshot(params,null)
    val reader = writableImage.getPixelReader
    val wIm = new WritableImage(width,height)
    val writer = wIm.getPixelWriter
    val data =
      if(!isGray) {
        //获取彩图，每个像素点4Byte
        val byteBuffer = ByteBuffer.allocate(4 * width * height)
        byteBuffer.clear()
        for (y <- 0 until height; x <- 0 until width) {
          val color = reader.getArgb(x, y)
          writer.setArgb(x,y,color)
          byteBuffer.putInt(color)
        }
        byteBuffer.flip() //翻转，修改lim为当前pos，pos置0
        val arrayOfByte = byteBuffer.array().take(byteBuffer.limit) //take前limit个Byte，去除Buffer内余下的多余数据
        ByteString.copyFrom(arrayOfByte)
      } else {
        //获取灰度图，每个像素点1Byte
        val byteArray = new Array[Byte](1 * width * height)
        for (y <- 0 until height; x <- 0 until width) {
          val color = reader.getColor(x, y).grayscale()
          val gray = color.getRed * color.getOpacity
          writer.setColor(x,y,new Color(gray,gray,gray,color.getOpacity))
          byteArray(y * height + x) = (gray * 255).toByte
        }
        ByteString.copyFrom(byteArray)
      }
    val pixelLength = if(isGray) 1 else 4
    (id, ImgData(width ,height, pixelLength, data))
  }

}
