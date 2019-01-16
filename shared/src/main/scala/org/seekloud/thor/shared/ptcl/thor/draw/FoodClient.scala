package org.seekloud.thor.shared.ptcl.thor.draw

import org.seekloud.thor.shared.ptcl.component.Food
import org.seekloud.thor.shared.ptcl.model.Point
import org.seekloud.thor.shared.ptcl.thor.ThorSchemaClientImpl
import org.seekloud.thor.shared.ptcl.model.Constants._
import org.seekloud.thor.shared.ptcl.util.middleware.{MiddleContext, MiddleFrame}

import scala.collection.mutable
import scala.util.Random

/**
  * Created by Jingyi on 2018/11/9
  */
trait FoodClient { this: ThorSchemaClientImpl =>
  //绘制食物

  def drawFood(offset: Point, canvasUnit: Float, canvasBoundary: Point): Unit = {

    preCanvasFood match{
      case Nil =>
        foodMap.foreach{food=>
          drawAFood(food._2, offset, canvasUnit,canvasBoundary)
//          if((System.currentTimeMillis() - preTime) % 2000 <= 30) println("no Pre first")
        }
      case _ =>
        if(System.currentTimeMillis() - preTime < 2000){
          foodMap.foreach{food=>
            drawAFood(food._2, offset, canvasUnit,canvasBoundary)
//            if((System.currentTimeMillis() - preTime) % 2000 <= 30) println("no Pre")
          }
        }
        else{
          foodMap.foreach{food=>
            drawFoodByPre(food._2, offset, canvasUnit,canvasBoundary)
//            if((System.currentTimeMillis() - preTime) % 2000 <= 30) println("Pre !!!")
          }

        }
    }
  }

  def drawAFood(food:Food, offset:Point, canvasUnit: Float, canvasBoundary: Point): Unit = {

    val img = drawFrame.createImage(pictureMap(s"food-sheet0-${food.getFoodState.color}.png"))

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

  def drawFoodByPre(food:Food, offset:Point, canvasUnit: Float, canvasBoundary: Point): Unit ={

    val color = food.getFoodState.color

    val r = config.getRadiusByFoodLevel(food.getFoodState.level)
    val sx = food.getFoodState.position.x - r + offset.x
    val sy = food.getFoodState.position.y - r + offset.y
    val dx = 2 * r
    val dy = 2 * r

    if(0 < sx && sx < canvasBoundary.x && 0 < sy && sy < canvasBoundary.y){
      //只绘制视角窗口内的食物
      ctx.save()
      ctx.drawImage(preCanvasFood(color), sx * canvasUnit, sy * canvasUnit, Some(dx * canvasUnit, dy * canvasUnit))
      ctx.restore()
    }
  }
}