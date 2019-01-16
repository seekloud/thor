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

  var foodCanvas: List[MiddleCanvas] = Nil

  var foodCtx: List[MiddleContext] = Nil

  canvasDrawFood()

  //预渲染人物
  val drawAdventurer = new MiddleFrameInJs
  val adventurerImg: List[MiddleImage] = for(a <- (0 to 19).toList) yield drawAdventurer.createImage(s"/img/char${a/4 + 1}-${a%4}.png")

  var adventurerCanvas: List[MiddleCanvas] = Nil

  var adventurerCtx: List[MiddleContext] = Nil

  canvasDrawAdventurer()

  //预渲染武器
  val drawWeapon = new MiddleFrameInJs
  val weaponImg: List[MiddleImage] = for(a <- (0 to 5).toList) yield drawWeapon.createImage(s"/img/weapon${a + 1}.png")

  var weaponCanvas: List[MiddleCanvas] = Nil

  var weaponCtx: List[MiddleContext] = Nil

  canvasDrawWeapon()

  def canvasDrawWeapon(): Unit ={
    var cnt = 0
    if(weaponImg.forall(t => t.isComplete)){
      val weaponHeight = for(a <- (0 to 5).toList) yield 250.0 / weaponImg(a).width * weaponImg(a).height
      for(a <- (0 to 5).toList) {weaponCanvas = weaponCanvas :+ drawWeapon.createCanvas(250.0, weaponHeight(a))}
//      println(weaponCanvas)
      for(a <- (0 to 5).toList) {weaponCtx = weaponCtx :+ weaponCanvas(a).getCtx}
      weaponCtx.foreach{ t =>
        t.drawImage(weaponImg(cnt),0 ,0, Some(250.0, weaponHeight(cnt)))
        cnt += 1
      }
    }
    else {
      dom.window.setTimeout(()=>
        canvasDrawWeapon()
        , 1000
      )
    }
  }
  def canvasDrawAdventurer(): Unit ={
    var cnt = 0
    if(adventurerImg.forall(t => t.isComplete)){
      for(a <- (0 to 19).toList) {adventurerCanvas = adventurerCanvas :+ drawAdventurer.createCanvas(150.0, 150.0)}
      for(a <- (0 to 19).toList) {adventurerCtx = adventurerCtx :+ adventurerCanvas(a).getCtx}
      adventurerCtx.foreach{ t =>
        t.drawImage(adventurerImg(cnt),0 ,0, Some(150 ,150))
        cnt += 1
      }
    }
    else {
      dom.window.setTimeout(()=>
        canvasDrawAdventurer()
        , 1000
      )
    }
  }
  def canvasDrawFood(): Unit ={
    var cnt = 0
    if(foodImg.forall(t => t.isComplete)){
      for(a <- (0 to 7).toList) {foodCanvas = foodCanvas :+ drawFood.createCanvas(48.0, 48.0)}
      for(a <- (0 to 7).toList) {foodCtx = foodCtx :+ foodCanvas(a).getCtx}
      foodCtx.foreach{ t =>
        t.drawImage(foodImg(cnt),0 ,0, Some(48 ,48))
        cnt += 1
      }
    }
    else {
      dom.window.setTimeout(()=>
        canvasDrawFood()
        , 1000
      )
    }
  }

}
