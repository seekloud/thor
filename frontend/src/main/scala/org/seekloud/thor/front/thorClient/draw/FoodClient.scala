package org.seekloud.thor.front.thorClient.draw

import org.seekloud.thor.front.common.Routes
import org.seekloud.thor.front.thorClient.ThorSchemaClientImpl
import org.seekloud.thor.shared.ptcl.component.Food
import org.seekloud.thor.shared.ptcl.model.Point
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLElement
import org.scalajs.dom.html

import scala.collection.mutable
import scala.util.Random

/**
  * Created by Jingyi on 2018/11/9
  */
trait FoodClient { this: ThorSchemaClientImpl =>

  //绘制食物

  def drawFood(offset: Point, canvasUnit: Float, canvasBoundary: Point): Unit = {

    def drawAFood(food:Food, offset:Point, canvasUnit: Float, canvasBoundary: Point): Unit = {

      val img = dom.document.createElement("img").asInstanceOf[html.Image]
      img.setAttribute("src", s"/thor/static/img/food-sheet0-${food.getFoodState.color}.png")

      val r = config.getRadiusByFoodLevel(food.getFoodState.level)
      val sx = food.getFoodState.position.x - r + offset.x
      val sy = food.getFoodState.position.y - r + offset.y
      val dx = 2 * r
      val dy = 2 * r

      if(0 < sx && sx < canvasBoundary.x && 0 < sy && sy < canvasBoundary.y){
        //只绘制视角窗口内的食物
        ctx.save()
        ctx.drawImage(img, sx * canvasUnit, sy * canvasUnit, dx * canvasUnit, dy * canvasUnit)
        ctx.restore()
      }

    }

    foodMap.foreach{food=>
      drawAFood(food._2, offset, canvasUnit,canvasBoundary)
    }
  }
}