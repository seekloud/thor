package org.seekloud.thor.front.common

import org.scalajs.dom
import org.seekloud.thor.front.utils.middleware.MiddleFrameInJs
import org.seekloud.thor.shared.ptcl.util.middleware.{MiddleCanvas, MiddleContext, MiddleImage}

/**
  * User: XuSiRan
  * Date: 2018/12/25
  * Time: 11:59
  */
class PreDraw {
  // 食物预渲染Canvas
  val drawFood = new MiddleFrameInJs
  val foodImg: List[MiddleImage] = for(a <- (0 to 7).toList) yield drawFood.createImage(s"/img/food-sheet0-$a.png")

  val canvas: List[MiddleCanvas] = for(a <- (0 to 7).toList) yield drawFood.createCanvas(48.0, 48.0)

  val foodCtx: List[MiddleContext] = for(a <- (0 to 7).toList) yield canvas(a).getCtx

  dom.window.setTimeout(()=>{
    var cnt = 0
    foodCtx.foreach{ t =>
      t.drawImage(foodImg(cnt),0 ,0, Some(48 ,48))
      cnt += 1
    }
  }
  , 1000)

}
