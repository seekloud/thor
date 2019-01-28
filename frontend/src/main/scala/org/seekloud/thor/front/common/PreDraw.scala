/*
 * Copyright 2018 seekloud (https://github.com/seekloud)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.seekloud.thor.front.common

import org.scalajs.dom
import org.seekloud.thor.front.utils.middleware.MiddleFrameInJs
import org.seekloud.thor.shared.ptcl.util.middleware.{MiddleCanvas, MiddleContext, MiddleImage}
import org.seekloud.thor.shared.ptcl.model.Constants._

/**
  * User: XuSiRan
  * Date: 2018/12/25
  * Time: 11:59
  */
class PreDraw {
  // 食物预渲染Canvas
  val drawFood = new MiddleFrameInJs
  val foodImg: List[MiddleImage] = for(a <- (0 to 7).toList) yield drawFood.createImage(pictureMap(s"food-sheet0-$a.png"))

  var foodCanvas: List[MiddleCanvas] = Nil

  var foodCtx: List[MiddleContext] = Nil

  canvasDrawFood()

  //预渲染人物
  val drawAdventurer = new MiddleFrameInJs
  val adventurerImg: List[MiddleImage] = for(a <- (0 to 19).toList) yield drawAdventurer.createImage(pictureMap(s"char${a/4 + 1}-${a%4}.png"))

  var adventurerCanvas: List[MiddleCanvas] = Nil

  var adventurerCtx: List[MiddleContext] = Nil

  canvasDrawAdventurer()

  //预渲染武器
  val drawWeapon = new MiddleFrameInJs
  val weaponImg: List[MiddleImage] = for(a <- (0 to 5).toList) yield drawWeapon.createImage(pictureMap(s"weapon${a + 1}.png"))

  var weaponCanvas: List[MiddleCanvas] = Nil

  var weaponCtx: List[MiddleContext] = Nil

  canvasDrawWeapon()

  def canvasDrawWeapon(): Unit ={
    var cnt = 0
    if(weaponImg.forall(t => t.isComplete)){
      val weaponHeight = for(a <- (0 to 5).toList) yield 250.0 / weaponImg(a).width * weaponImg(a).height
      for(a <- (0 to 5).toList) {weaponCanvas = weaponCanvas :+ drawWeapon.createCanvas(250.0, weaponHeight(a).toInt)}
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
