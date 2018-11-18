package com.neo.sk.thor.front.thorClient.draw

import com.neo.sk.thor.front.common.Routes
import com.neo.sk.thor.front.thorClient.ThorSchemaClientImpl
import com.neo.sk.thor.shared.ptcl.`object`.Food
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLElement
import org.scalajs.dom.html

import scala.collection.mutable

/**
  * Created by Jingyi on 2018/11/9
  */
trait FoodClient { this: ThorSchemaClientImpl =>
  private val foodImg1 = "thor/static/img/food-sheet0.png"
  private val foodImg2 = "thor/static/img/food-sheet1.png"
  private val foodImg3 = "thor/static/img/food-sheet2.png"

  private def generateFood(food:Food) = {
//    val foodCanvas = dom.document.createElement("foodCanvas").asInstanceOf[html.Canvas]
//    val foodCtx = foodCanvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
    val img = food.level match {
      case 1 => foodImg1
      case 2 => foodImg2
      case 3 => foodImg3
    }
    val mapImg = dom.document.createElement("img").asInstanceOf[html.Image]
    mapImg.setAttribute("src", s"${img}")
    val r = food.getFoodState.radius
    val sx = food.getFoodState.position.x - r
    val sy = food.getFoodState.position.y - r
    val dx = sx + 2 * r
    val dy = sy + 2 * r
    ctx.save()
    ctx.drawImage(mapImg, sx, sy, dx, dy)
    ctx.restore()
  }
  def drawFood() = {
    foodMap.map{foods=>
      generateFood(foods._2)
    }

  }

  def drawFoodByOffsetTime(offSetTime:Long) = {

  }
}