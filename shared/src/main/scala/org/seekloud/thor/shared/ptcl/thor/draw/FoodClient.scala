package org.seekloud.thor.shared.ptcl.thor.draw

import org.seekloud.thor.shared.ptcl.component.Food
import org.seekloud.thor.shared.ptcl.model.Point
import org.seekloud.thor.shared.ptcl.thor.ThorSchemaClientImpl

import scala.collection.mutable
import scala.util.Random

/**
  * Created by Jingyi on 2018/11/9
  */
trait FoodClient { this: ThorSchemaClientImpl =>

  //绘制食物

  def drawFood(offset: Point, canvasUnit: Float, canvasBoundary: Point): Unit = {

    def drawAFood(food:Food, offset:Point, canvasUnit: Float, canvasBoundary: Point): Unit = {

      val img = drawFrame.createImage(s"/img/food-sheet0-${food.getFoodState.color}.png")

      val r = config.getRadiusByFoodLevel(food.getFoodState.level)
      val sx = food.getFoodState.position.x - r + offset.x
      val sy = food.getFoodState.position.y - r + offset.y
      val dx = 2 * r
      val dy = 2 * r

      if(0 < sx && sx < canvasBoundary.x && 0 < sy && sy < canvasBoundary.y){
        //只绘制视角窗口内的食物
        ctx.save()
        ctx.drawImage(img, sx * canvasUnit, sy * canvasUnit, Some(dx * canvasUnit, dy * canvasUnit))
        ctx.restore()
      }

    }

    foodMap.foreach{food=>
      drawAFood(food._2, offset, canvasUnit,canvasBoundary)
    }
  }
}