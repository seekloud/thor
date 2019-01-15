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
  , 10000)

  //预渲染人物
  val drawAdventurer = new MiddleFrameInJs
  val adventurerImg: List[MiddleImage] = for(a <- (0 to 19).toList) yield drawAdventurer.createImage(s"/img/char${a/4 + 1}-${a%4}.png")

  val adventurerCanvas: List[MiddleCanvas] = for(a <- (0 to 19).toList) yield drawAdventurer.createCanvas(150.0, 150.0)

  val adventurerCtx: List[MiddleContext] = for(a <- (0 to 19).toList) yield adventurerCanvas(a).getCtx

  dom.window.setTimeout(()=>{
    var cnt = 0
    adventurerCtx.foreach{ t =>
      t.drawImage(adventurerImg(cnt),0 ,0, Some(150 ,150))
      cnt += 1
    }
  }
  , 10000)

  //预渲染武器
  val drawWeapon = new MiddleFrameInJs
  val weaponImg: List[MiddleImage] = for(a <- (0 to 5).toList) yield drawWeapon.createImage(s"/img/weapon${a + 1}.png")

  val canvasHeight: List[Float] = for(a <- (0 to 5).toList) yield (250.0 / weaponImg(a).width * weaponImg(a).height).toFloat

  val weaponCanvas: List[MiddleCanvas] = for(a <- (0 to 5).toList) yield drawWeapon.createCanvas(500.0, 120)

  val weaponCtx: List[MiddleContext] = for(a <- (0 to 5).toList) yield weaponCanvas(a).getCtx

  dom.window.setTimeout(()=>{
    var cnt = 0
    weaponCtx.foreach{ t =>
      t.drawImage(weaponImg(cnt),0 ,0)
      cnt += 1
    }
  }
    , 10000)

}
