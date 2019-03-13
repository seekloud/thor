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

import javafx.scene.image.Image
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
  
  val foodImg: List[Image] = for(a <- (0 to 7).toList) yield drawFood.createImage(pictureMap(s"food-sheet0-$a.png")).getImage

  var foodCanvas: List[MiddleCanvas] = Nil

  var foodCtx: List[MiddleContext] = Nil

  canvasDrawFood()


  // 死亡特效预渲染Canvas
  val drawDeath = new MiddleFrameInFx
  
  val deathImg: List[Image] = for(a <- (1 to 6).toList) yield drawDeath.createImage(pictureMap(s"kill$a.png")).getImage

  var deathCanvas: List[MiddleCanvas] = Nil

  var deathCtx: List[MiddleContext] = Nil

  canvasDrawDeath()

  //预渲染人物
  val drawAdventurer = new MiddleFrameInFx
  
  val adventurerImg: List[Image] = for(a <- (0 to 19).toList) yield drawAdventurer.createImage(pictureMap(s"char${a/4 + 1}-${a%4}.png")).getImage

  var adventurerCanvas: List[MiddleCanvas] = Nil

  var adventurerCtx: List[MiddleContext] = Nil

  canvasDrawAdventurer()

  //预渲染武器
  val drawWeapon = new MiddleFrameInFx
  
  val weaponImg: List[Image] = for(a <- (0 to 5).toList) yield drawWeapon.createImage(pictureMap(s"weapon${a + 1}.png")).getImage

  var weaponCanvas: List[MiddleCanvas] = Nil

  var weaponCtx: List[MiddleContext] = Nil

  canvasDrawWeapon()

  def canvasDrawWeapon(): Unit ={
    if(!weaponImg.forall(t => t.isBackgroundLoading)){
      val timer = new Timer
      val timerTask = new TimerTask {
        override def run(): Unit = canvasDrawWeapon()
      }
      timer.schedule(timerTask, 1000)
    }
  }
  def canvasDrawAdventurer(): Unit ={
    if(!adventurerImg.forall(t => t.isBackgroundLoading)){
      val timer = new Timer
      val timerTask = new TimerTask {
        override def run(): Unit = canvasDrawAdventurer()
      }
      timer.schedule(timerTask, 1000)
    }
  }
  def canvasDrawFood(): Unit ={
    if(!foodImg.forall(t => t.isBackgroundLoading)){
      val timer = new Timer
      val timerTask = new TimerTask {
        override def run(): Unit = canvasDrawFood()
      }
      timer.schedule(timerTask, 1000)
    }
  }


  def canvasDrawDeath(): Unit ={
    if(!deathImg.forall(t => t.isBackgroundLoading)){
      val timer = new Timer
      val timerTask = new TimerTask {
        override def run(): Unit = canvasDrawFood()
      }
      timer.schedule(timerTask, 1000)
    }
  }
  
}
