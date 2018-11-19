package com.neo.sk.thor.front.thorClient.draw

import com.neo.sk.thor.front.common.Routes
import com.neo.sk.thor.front.thorClient.ThorSchemaClientImpl
import com.neo.sk.thor.shared.ptcl.`object`.Food
import com.neo.sk.thor.shared.ptcl.model.Point
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLElement
import org.scalajs.dom.html

import scala.collection.mutable
import scala.util.Random

/**
  * Created by Jingyi on 2018/11/9
  */
trait FoodClient { this: ThorSchemaClientImpl =>

  val random = new Random(System.currentTimeMillis())

  private def foodImg1 = s"/thor/static/img/food-sheet0-1.png"
  private val foodImg2 = "/thor/static/img/food-sheet1.png"
  private val foodImg3 = "/thor/static/img/food-sheet2.png"

  private def generateFood(food:Food, offset:Point, canvasUnit: Int, canvasBoundary: Point) = {
//    val foodCanvas = dom.document.createElement("foodCanvas").asInstanceOf[html.Canvas]
//    val foodCtx = foodCanvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
//    val img = food.level match {
//      case 1 => foodImg1
//      case 2 => foodImg1
//      case _ => foodImg3
//    }
    val mapImg = dom.document.createElement("img").asInstanceOf[html.Image]
    mapImg.setAttribute("src", s"/thor/static/img/food-sheet0-${food.getFoodState.color}.png")
//    val r = food.getFoodState.radius
    val r = config.getRadiusByFoodLevel(food.getFoodState.level)
    val sx = food.getFoodState.position.x - r + offset.x
    val sy = food.getFoodState.position.y - r + offset.y
    val dx = 2 * r
    val dy = 2 * r

    if(0 < sx && sx < canvasBoundary.x && 0 < sy && sy < canvasBoundary.y){
      ctx.save()
      ctx.drawImage(mapImg, sx * canvasUnit, sy * canvasUnit, dx * canvasUnit, dy * canvasUnit)
      ctx.restore()
    }

  }
  def drawFood() = {


  }

  def drawFoodByOffsetTime(offset: Point, canvasUnit: Int, canvasBoundary: Point) = {
    foodMap.map{foods=>
      generateFood(foods._2, offset, canvasUnit,canvasBoundary)
    }
  }
}