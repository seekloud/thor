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

import java.util.Timer
import java.util.TimerTask
import org.seekloud.thor.utils.middleware.MiddleFrameInFx
import org.seekloud.thor.shared.ptcl.util.middleware.{MiddleCanvas, MiddleContext, MiddleImage}
import org.seekloud.thor.model.Constants._

/**
  * User: Jason
  * Date: 2019/3/9
  * Time: 12:55
  */
class PreDraw {

  // 食物预渲染Canvas
  val drawFood = new MiddleFrameInFx
  
  val foodImg: List[MiddleImage] = for(a <- (0 to 7).toList) yield drawFood.createImage(pictureMap(s"food-sheet0-$a.png"))

  var foodCanvas: List[MiddleCanvas] = Nil

  var foodCtx: List[MiddleContext] = Nil

  canvasDrawFood()

  // 加速特效预渲染Canvas
  val drawSpeedUp = new MiddleFrameInFx
  
  val speedUpImg: List[MiddleImage] = for(a <- (0 to 7).toList) yield drawSpeedUp.createImage(pictureMap(s"speedParticles.png"))

  var speedUpCanvas: List[MiddleCanvas] = Nil

  var speedUpCtx: List[MiddleContext] = Nil

  canvasDrawSpeedUp()

  // 死亡特效预渲染Canvas
  val drawDeath = new MiddleFrameInFx
  
  val deathImg: List[MiddleImage] = for(a <- (1 to 6).toList) yield drawDeath.createImage(pictureMap(s"kill$a.png"))

  var deathCanvas: List[MiddleCanvas] = Nil

  var deathCtx: List[MiddleContext] = Nil

  canvasDrawDeath()

  //预渲染人物
  val drawAdventurer = new MiddleFrameInFx
  
  val adventurerImg: List[MiddleImage] = for(a <- (0 to 19).toList) yield drawAdventurer.createImage(pictureMap(s"char${a/4 + 1}-${a%4}.png"))

  var adventurerCanvas: List[MiddleCanvas] = Nil

  var adventurerCtx: List[MiddleContext] = Nil

  canvasDrawAdventurer()

  //预渲染武器
  val drawWeapon = new MiddleFrameInFx
  
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
      val timer = new Timer
      val timerTask = new TimerTask {
        override def run(): Unit = canvasDrawWeapon()
      }
      timer.schedule(timerTask, 1000)
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
      val timer = new Timer
      val timerTask = new TimerTask {
        override def run(): Unit = canvasDrawAdventurer()
      }
      timer.schedule(timerTask, 1000)
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
      val timer = new Timer
      val timerTask = new TimerTask {
        override def run(): Unit = canvasDrawFood()
      }
      timer.schedule(timerTask, 1000)
    }
  }

  def canvasDrawSpeedUp(): Unit ={
    var cnt = 0
    if(speedUpImg.forall(t => t.isComplete)){
      for(a <- (0 to 7).toList) {speedUpCanvas = speedUpCanvas :+ drawSpeedUp.createCanvas(48.0, 48.0)}
      for(a <- (0 to 7).toList) {speedUpCtx = speedUpCtx :+ speedUpCanvas(a).getCtx}
      speedUpCtx.foreach{ t =>
        t.drawImage(speedUpImg(cnt),0 ,0, Some(140 ,160))
        cnt += 1
      }
    }
    else {
      val timer = new Timer
      val timerTask = new TimerTask {
        override def run(): Unit = canvasDrawFood()
      }
      timer.schedule(timerTask, 1000)
    }
  }

  def canvasDrawDeath(): Unit ={
    var cnt = 0
    if(deathImg.forall(t => t.isComplete)){
      for(a <- (0 to 5).toList) {deathCanvas = deathCanvas :+ drawDeath.createCanvas(200,190)}
      for(a <- (0 to 5).toList) {deathCtx = deathCtx :+ deathCanvas(a).getCtx}
      deathCtx.foreach{ t =>
        t.drawImage(deathImg(cnt),0 ,0, Some(200,190))
        cnt += 1
      }
    }
    else {
      val timer = new Timer
      val timerTask = new TimerTask {
        override def run(): Unit = canvasDrawFood()
      }
      timer.schedule(timerTask, 1000)
    }
  }
  
}
