/*
 *   Copyright 2018 seekloud (https://github.com/seekloud)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.seekloud.thor.scene

import javafx.scene.image.Image
import org.seekloud.thor.shared.ptcl.component.{Adventurer, Food, FoodState}
import org.seekloud.thor.model.Constants.pictureMap
import org.seekloud.thor.shared.ptcl.model.{Point, Score}
import org.seekloud.thor.shared.ptcl.thor.ThorSchemaClientImpl
import org.seekloud.thor.shared.ptcl.util.middleware.MiddleCanvas
import org.seekloud.thor.utils.CanvasUtils

/**
  * User: Jason
  * Date: 2019/3/10
  * Time: 15:20
  */
class DrawScene(impl: ThorSchemaClientImpl) {

  /*energyBar conf*/
  private val barLength = 500
  private val barHeight = 25
  private val r = barHeight / 2

  private def barLeft = (impl.canvasSize.x - barLength) / 2

  private def barTop = impl.canvasSize.y - barHeight - 50

  val mapImg = new Image("img/background.png")
  val hammerImg = new Image("img/hammer.png")
  val starImg = new Image("img/star.png")

  def drawGame4Client(mainId: String, offSetTime: Long, canvasUnit: Float, canvasBounds: Point): Unit = {
    if (!impl.waitSyncData) {
      impl.adventurerMap.get(mainId) match {
        case Some(adventurer) =>
          //保持自己的adventurer在屏幕中央~
          val moveDistance = getMoveDistance(adventurer, offSetTime)
          val offset = canvasBounds / 2 - (adventurer.getAdventurerState.position + moveDistance)
          val a = System.currentTimeMillis()
          drawBackground(offset, canvasUnit, canvasBounds)
          val b = System.currentTimeMillis()
          if (b-a>5)
          println(s"draw background time span: ${b-a}")
          drawFood(offset, canvasUnit, canvasBounds)
          val c = System.currentTimeMillis()
          if (c-b>5)
            println(s"draw food time span: ${c-b}")
          drawAdventurers(offSetTime, offset, canvasUnit, canvasBounds)
          val d = System.currentTimeMillis()
          if (d-c>5)
            println(s"draw adventurer time span: ${d-c}")
          drawBodyFood(offset, offSetTime, canvasUnit, canvasBounds)
          val e = System.currentTimeMillis()
          if (e-d>5)
            println(s"draw body food time span: ${e-d}")

          drawEnergyBar(adventurer)
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

  def drawBackground(offset: Point, canvasUnit: Float, canvasBoundary: Point): Unit = {
    impl.ctx.save()
    impl.ctx.setFill("#171b1f")
    impl.ctx.fillRec(0, 0, canvasBoundary.x * canvasUnit, canvasBoundary.y * canvasUnit)
    impl.ctx.fill()
    impl.ctx.drawImage(mapImg, offset.x * canvasUnit, offset.y * canvasUnit, Some(impl.config.boundary.x / 2 * canvasUnit, impl.config.boundary.y / 2 * canvasUnit))
    impl.ctx.drawImage(mapImg, (offset.x + impl.config.boundary.x / 2) * canvasUnit, offset.y * canvasUnit, Some(impl.config.boundary.x / 2 * canvasUnit, impl.config.boundary.y / 2 * canvasUnit))
    impl.ctx.drawImage(mapImg, offset.x * canvasUnit, (offset.y + impl.config.boundary.y / 2) * canvasUnit, Some(impl.config.boundary.x / 2 * canvasUnit, impl.config.boundary.y / 2 * canvasUnit))
    impl.ctx.drawImage(mapImg, (offset.x + impl.config.boundary.x / 2) * canvasUnit, (offset.y + impl.config.boundary.y / 2) * canvasUnit, Some(impl.config.boundary.x / 2 * canvasUnit, impl.config.boundary.y / 2 * canvasUnit))
    val borderW = 10
    impl.ctx.setFill("#4A4B49")
    impl.ctx.rect(offset.x * canvasUnit, offset.y * canvasUnit, impl.config.boundary.x * canvasUnit, borderW)
    impl.ctx.rect(offset.x * canvasUnit, offset.y * canvasUnit, borderW, impl.config.boundary.y * canvasUnit)
    impl.ctx.rect((offset.x + impl.config.boundary.x) * canvasUnit - borderW, offset.y * canvasUnit, borderW, impl.config.boundary.y * canvasUnit)
    impl.ctx.rect(offset.x * canvasUnit, (offset.y + impl.config.boundary.y) * canvasUnit - borderW, impl.config.boundary.x * canvasUnit, borderW)

    impl.ctx.fill()
    impl.ctx.restore()
  }

  def drawBarrage(s: String, t: String): Unit = {
    impl.ctx.save()
    impl.ctx.setFont("Comic Sans Ms", 25)
    impl.ctx.setTextBaseLine("top")
    impl.ctx.setFill("#ffffff")
    if (t == "join") {
      println("join")
      val tmp = s + "加入了游戏"
      impl.ctx.fillText(tmp, impl.window.x * 0.38, impl.window.y * 0.17)
    }
    else if (t == "left") {
      println("left")
      val tmp = s + "离开了游戏"
      impl.ctx.fillText(tmp, impl.window.x * 0.38, impl.window.y * 0.17)
    }
    else {
      val start = impl.window.x * 0.5 - (impl.ctx.measureText(s"$s $t") + 80) / 2
      impl.ctx.fillText(s, start, impl.window.y * 0.17)
      impl.ctx.drawImage(hammerImg, start + impl.ctx.measureText(s) + 25, impl.window.y * 0.15, Some(50, 50))
      impl.ctx.fillText(t, start + impl.ctx.measureText(s) + 150, impl.window.y * 0.17)
    }

    impl.ctx.restore()
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

  def drawAdventurer(offSetTime: Long, offset: Point, canvasUnit: Float, canvasBoundary: Point): Unit = {

    def drawAnAdventurer(adventurer: Adventurer): Unit = {
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

        CanvasUtils.rotateImage("speed", impl.drawFrame, impl.ctx, Nil, Nil, "img/speedparticles.png", Point(sx, sy) * canvasUnit, Point(-height, 0), width, height, adventurer.getAdventurerState.direction, impl.preTime, adventurer.getAdventurerState.level)
      }

      //画人物
      //      val drawX = if(systemFrame%6 < 3) dx * 0.98.toFloat else dx
      if (impl.ifTest) {
        impl.ctx.save()
        impl.ctx.setFill("#FF0000")
        impl.ctx.beginPath()
        impl.ctx.arc(sx * canvasUnit, sy * canvasUnit, r * canvasUnit, 0, 2 * Math.PI, counterclockwise = false)
        impl.ctx.closePath()
        impl.ctx.fill()
        impl.ctx.restore()
      }
      CanvasUtils.rotateImage("adventurer", impl.drawFrame, impl.ctx, impl.preCanvasAdventurer, impl.preAdventurerImage, pictureMap(s"char${(adventurer.level % 21 - 1) / 4 + 1}-${(adventurer.level - 1) % 4}.png"), Point(sx, sy) * canvasUnit, Point(0, 0), dx * canvasUnit * 1.1.toFloat, 0, adventurer.getAdventurerState.direction, impl.preTime, adventurer.getAdventurerState.level)
      //        println(s"arc:${r * canvasUnit} img:${dx * canvasUnit * 0.85.toFloat}")
      //出生保护
      impl.newbornAdventurerMap.get(adventurer.playerId) match {
        case Some(s) =>
          impl.ctx.save()
          impl.ctx.setFill("rgba(79,148,205,0.4)")
          impl.ctx.setGlobalAlpha(0.8)
          impl.ctx.beginPath()
          impl.ctx.arc(sx * canvasUnit, sy * canvasUnit, r * canvasUnit * 1.15, 0, 360, counterclockwise = false)
          impl.ctx.closePath()
          impl.ctx.fill()
          impl.ctx.restore()
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
      CanvasUtils.rotateImage("weapon", impl.drawFrame, impl.ctx, impl.preCanvasWeapon, impl.preWeaponImage, pictureMap(s"weapon${(adventurer.level - 1) / 4 + 1}.png"), weaponPosition * canvasUnit, Point(0, 0), weaponLength * canvasUnit, 0, angle + move, impl.preTime, adventurer.getAdventurerState.level)
      //用户昵称
      impl.ctx.save()
      impl.ctx.setFill("#ffffff")
      impl.ctx.setTextAlign("center")
      impl.ctx.setFont("微软雅黑", 20)
      impl.ctx.setTextBaseLine("top")
      impl.ctx.fillText(s"${adventurer.name}", sx * canvasUnit, (sy + r) * canvasUnit + 20)
      impl.ctx.restore()

      //荣誉星号
      impl.ctx.save()
      if (adventurer.stickKillNum > 0) {
        for (i <- 1 to adventurer.stickKillNum)
          impl.ctx.drawImage(starImg, (sx + i * 2) * canvasUnit - 35, (sy + r) * canvasUnit + 45, Some(20, 20))
      }
      impl.ctx.restore()
    }

    impl.adventurerMap.foreach {
      adventurer =>
        if (!impl.dyingAdventurerMap.contains(adventurer._1)) {
          drawAnAdventurer(adventurer._2)
        }
    }
  }

  def drawDying(offset: Point, offsetTime: Long, canvasUnit: Float, preCanvas: List[MiddleCanvas] = Nil, preImage: List[Image] = Nil): Any = {

    def drawADying(adventurer: Adventurer, step: Int): Any = {

      val position = adventurer.getAdventurerState.position
      val r = impl.config.getAdventurerRadiusByLevel(adventurer.level)

      val o = if (offsetTime.toFloat / impl.config.frameDuration > 0.5) 1 else 0

      //根据进度选取死亡动画 step -> 2,1,0 img -> 1~6

      preImage match {
        case Nil =>
          preCanvas match {
            case Nil =>
              val img = impl.drawFrame.createImage(pictureMap(s"kill${5 - step * 2 + o}.png"))

              val width = img.width
              val height = img.height

              //      val canvasCache = impl.drawFrame.createCanvas(math.ceil(width * canvasUnit).toInt, math.ceil(height * canvasUnit).toInt)
              //      val impl.ctxCache = canvasCache.getCtx

              impl.ctx.drawImage(img, (position.x + offset.x) * canvasUnit - width / 2, (position.y + offset.y) * canvasUnit - height / 2)
            case _ =>
              val img = preCanvas(5 - step * 2 + o - 1)
              //                impl.drawFrame.createImage(pictureMap(s"kill${5 - step*2 + o}.png"))
              val width = img.getWidth()
              val height = img.getHeight()

              impl.ctx.drawImage(img, (position.x + offset.x) * canvasUnit - width / 2, (position.y + offset.y) * canvasUnit - height / 2)
          }
        case _ =>
          val img = preImage(5 - step * 2 + o - 1)
          //                impl.drawFrame.createImage(pictureMap(s"kill${5 - step*2 + o}.png"))
          val width = img.getWidth
          val height = img.getHeight

          impl.ctx.drawImage(img, (position.x + offset.x) * canvasUnit - width / 2, (position.y + offset.y) * canvasUnit - height / 2)
      }
      //      canvasCache.change2Image()
    }

    impl.dyingAdventurerMap.foreach {
      adventurer =>
        drawADying(adventurer._2._1, adventurer._2._2)
    }
  }

  def drawLevelUp(adventurer: Adventurer, step: Int, offSetTime: Long, offset: Point, canvasUnit: Float): Unit = {

    if (adventurer.isUpdateLevel) {

      val img = impl.drawFrame.createImage("img/level-up.png")

      val width = img.width
      val height = img.height
      val position = adventurer.getAdventurerState.position
      val r = impl.config.getAdventurerRadiusByLevel(adventurer.level)
      val moveDistance = getMoveDistance(adventurer, offSetTime)

      val movePerStep = 15 //每个逻辑帧的高度位移
      val offsetHeight = -200 + movePerStep * (step - offSetTime.toFloat / impl.config.frameDuration) //每个渲染帧的高度偏移量
      val opacity = 0.1 * step //透明度逐渐增加

      impl.ctx.save()
      impl.ctx.setGlobalAlpha(opacity)
      impl.ctx.drawImage(img, (position.x + offset.x + moveDistance.x) * canvasUnit - width / 2, (position.y + offset.y + moveDistance.y - r) * canvasUnit - height / 2 + offsetHeight)
      impl.ctx.restore()
    }
  }


  def drawAdventurers(offSetTime: Long, offset: Point, canvasUnit: Float, canvasBoundary: Point): Unit = {

    drawAdventurer(offSetTime, offset, canvasUnit, canvasBoundary)

    drawDying(offset, offSetTime, canvasUnit, impl.preCanvasDeath, impl.preDeathImage)

    impl.adventurerMap.get(impl.myId).foreach {
      adventurer =>
        drawLevelUp(adventurer, adventurer.getAdventurerState.levelUpExecute, offSetTime, offset, canvasUnit)
    }
  }

  def drawArcRect(r: Double, width: Double, height: Double, left: Double, top: Double): Unit = {
    if (width >= 2 * r) {
      impl.ctx.save()
      impl.ctx.beginPath()
      impl.ctx.arc(left + r, top + r, r, 90, 270, counterclockwise = false)
      impl.ctx.lineTo(left + width - r, top + height)
      impl.ctx.arc(left + width - r, top + r, r, 270, 450, counterclockwise = false)
      impl.ctx.lineTo(left + r, top)
      impl.ctx.closePath()
      impl.ctx.fill()
      impl.ctx.restore()
    }
  }

  def drawEnergyBar(adventurer: Adventurer): Unit = {
    impl.ctx.save()

    impl.ctx.setFill("#FFFFFF")
    drawArcRect(r, barLength, barHeight, barLeft, barTop)

    impl.ctx.setFill("#FF0000")
    val preLevel = if (adventurer.level == 1) 0 else impl.config.getMaxEnergyByLevel((adventurer.level - 1).toByte)
    val nowLevel = impl.config.getMaxEnergyByLevel(adventurer.level)
    val fillLength = math.min((adventurer.energy - preLevel).toFloat / (nowLevel - preLevel) * barLength, barLength)
    drawArcRect(r - 1, fillLength - 2, barHeight - 2, barLeft + 1, barTop + 1)

    impl.ctx.setFill("#FFFFFF")
    impl.ctx.setTextAlign("center")
    impl.ctx.setFont("Comic Sans Ms", 36)
    impl.ctx.setTextBaseLine("top")
    impl.ctx.fillText(adventurer.level.toString, barLeft - 32, barTop)
    impl.ctx.restore()

    impl.ctx.restore()
  }

  def drawFood(offset: Point, canvasUnit: Float, canvasBoundary: Point): Unit = {
    impl.preFoodImage match {
      case Nil =>
        impl.preCanvasFood match {
          case Nil =>
            impl.foodMap.foreach { food =>
              drawAFood(food._2, offset, canvasUnit, canvasBoundary)
              //          if((System.currentTimeMillis() - preTime) % 2000 <= 30) println("no Pre first")
            }
          case _ =>
            if (System.currentTimeMillis() - impl.preTime < 2000) {
              impl.foodMap.foreach { food =>
                //            drawAFood(food._2, offset, canvasUnit, canvasBoundary)
                //            if((System.currentTimeMillis() - preTime) % 2000 <= 30) println("no Pre")
              }
            }
            else {
              impl.foodMap.foreach { food =>
                drawFoodByPre(food._2, offset, canvasUnit, canvasBoundary)
                //            if((System.currentTimeMillis() - preTime) % 2000 <= 30) println("Pre !!!")
              }

            }
        }
      case _ => impl.foodMap.foreach { food =>
        drawFoodByPreImage(food._2, offset, canvasUnit, canvasBoundary)
        //          if((System.currentTimeMillis() - preTime) % 2000 <= 30) println("no Pre first")
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
      impl.ctx.save()
      impl.ctx.drawImage(img, sx * canvasUnit, sy * canvasUnit, Some(dx * canvasUnit, dy * canvasUnit))
      impl.ctx.restore()
    }

  }

  def drawFoodByPre(food: Food, offset: Point, canvasUnit: Float, canvasBoundary: Point): Unit = {

    val color = food.getFoodState.color

    val r = impl.config.getRadiusByFoodLevel(food.getFoodState.level)
    val sx = food.getFoodState.position.x - r + offset.x
    val sy = food.getFoodState.position.y - r + offset.y
    val dx = 2 * r
    val dy = 2 * r

    if (0 < sx && sx < canvasBoundary.x && 0 < sy && sy < canvasBoundary.y) {
      //只绘制视角窗口内的食物
      impl.ctx.save()
      impl.ctx.drawImage(impl.preCanvasFood(color), sx * canvasUnit, sy * canvasUnit, Some(dx * canvasUnit, dy * canvasUnit))
      impl.ctx.restore()
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
      impl.ctx.save()
      impl.ctx.drawImage(impl.preFoodImage(color), sx * canvasUnit, sy * canvasUnit, Some(dx * canvasUnit, dy * canvasUnit))
      impl.ctx.restore()
    }
  }

  def baseFont: Float = impl.window.x / 1440

  def drawTextLine(str: String, x: Double, lineNum: Int, lineBegin: Int = 0 , tp:Int): Unit = {
    impl.ctx.save()
    impl.ctx.setTextBaseLine("top")
    if (tp == 1)
      impl.ctx.setFont("Comic Sans MS", baseFont * 20)
    else if (tp == 2){
      impl.ctx.setFont("Comic Sans MS", baseFont * 16)
      impl.ctx.setFill("#fdffff")}
    else{
      impl.ctx.setFont("Comic Sans MS", baseFont * 16)
      impl.ctx.setFill("#ffff00")
    }

    impl.ctx.fillText(str, x, (lineNum + lineBegin) * impl.window.x * 0.01)
    impl.ctx.restore()
  }

  def drawRank(Rank: List[Score], CurrentOrNot: Boolean, shortId: Byte): Unit = {
    val text = "———————排行榜———————"
    val RankBaseLine = 3
    var index = 0
    var yourRank = 100
    var yourNameIn = false
    val begin = if (CurrentOrNot) 10 else impl.window.x * 0.78
    var last = ""
    impl.ctx.save()
    impl.ctx.setFill("rgba(0,0,0,0.6)")
    impl.ctx.fillRec(begin, 0, impl.window.x * 0.25, impl.window.x * 0.25)
    impl.ctx.setFill("#fdffff")
    impl.ctx.setTextAlign("start")
    drawTextLine(s"   $text", 150 + impl.window.x * 0.01, 0, 2, 1)

    Rank.foreach { score =>
      index += 1
      if (score.bId == shortId) yourRank = index
      if (impl.playerIdMap.exists(_._1 == score.bId)) {
        val fullName = impl.playerIdMap(score.bId)._2
        val name = if (fullName.length <= 4) fullName.take(4) else fullName.take(4) + "..."
        if (index < 10) {
          impl.ctx.setTextAlign("left")
          if (score.bId == shortId) {
            yourNameIn = true
            drawTextLine(s"【$index】  $name ", begin + impl.window.x * 0.01, index * 2, RankBaseLine, 3)
            drawTextLine(s" 分数: ${score.e}", begin + impl.window.x * 0.11, index * 2, RankBaseLine, 3)
            drawTextLine(s" 击杀数: ${score.k}", begin + impl.window.x * 0.18, index * 2, RankBaseLine, 3)
          }
          else {
            drawTextLine(s"【$index】  $name ", begin + impl.window.x * 0.01, index * 2, RankBaseLine, 2)
            drawTextLine(s" 分数: ${score.e}", begin + impl.window.x * 0.11, index * 2, RankBaseLine, 2)
            drawTextLine(s" 击杀数: ${score.k}", begin + impl.window.x * 0.18, index * 2, RankBaseLine, 2)
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
            drawTextLine(s"【$yourRank】  $name ", begin + impl.window.x * 0.01, 20, RankBaseLine, 3)
            drawTextLine(s" 分数: ${yourScore.e}", begin + impl.window.x * 0.11, 20, RankBaseLine, 3)
            drawTextLine(s" 击杀数: ${yourScore.k}", begin + impl.window.x * 0.18, 20, RankBaseLine, 3)
          }
        case None =>
      }
    }
    impl.ctx.restore()
  }

  def drawSmallMap(mainId: String): Unit ={

    def drawStar(adventurerMapX: Double, adventurerMapY: Double): Unit = {
      impl.ctx.save()
      impl.ctx.beginPath()
      impl.ctx.moveTo(adventurerMapX - 6.6, adventurerMapY - 2.0)
      impl.ctx.lineTo(adventurerMapX + 6.6, adventurerMapY - 2.0)
      impl.ctx.lineTo(adventurerMapX - 4.0, adventurerMapY + 6.0)
      impl.ctx.lineTo(adventurerMapX - 0.0, adventurerMapY - 7.0)
      impl.ctx.lineTo(adventurerMapX + 4.0, adventurerMapY + 6.0)
      impl.ctx.setFill("#b1cfed")
      impl.ctx.fill()
      impl.ctx.restore()
    }

    def drawCrown(adventurerMapX: Double, adventurerMapY: Double): Unit = {
      val img = impl.drawFrame.createImage(pictureMap("crown.png"))
      impl.ctx.save()
      impl.ctx.beginPath()
      impl.ctx.drawImage(img, adventurerMapX - 6, adventurerMapY - 6, Some(12,12))
      impl.ctx.restore()
    }


    //获取比例
    val scale = (impl.window.x * 0.2) / 600.0
    impl.ctx.save()
    impl.ctx.setFill("rgba(0,0,0,0.4)")
    impl.ctx.fillRec(10, impl.window.y - impl.window.x * 0.11, impl.window.x * 0.2, impl.window.x * 0.1)
    impl.adventurerMap.foreach{
      case adventurer if adventurer._1 == mainId =>
        val adventurerMapX = 10 + adventurer._2.position.x * scale
        val adventurerMapY = impl.window.y - impl.window.x * 0.11 + adventurer._2.position.y * scale
        drawStar(adventurerMapX, adventurerMapY)
      case adventurer if adventurer._2.level >= 21 =>
        val adventurerMapX = 10 + adventurer._2.position.x * scale
        val adventurerMapY =  impl.window.y - impl.window.x * 0.11 + adventurer._2.position.y * scale
        drawCrown(adventurerMapX, adventurerMapY)
      case _ => ()
    }
    impl.ctx.restore()
  }

}
