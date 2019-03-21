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

package org.seekloud.thor.model

import scala.collection.mutable

/**
  * User: TangYaruo
  * Date: 2019/3/7
  * Time: 21:08
  */
object Constants {

  val CanvasWidth = 800
  val CanvasHeight = 400

  val layeredCanvasWidth = 200
  val layeredCanvasHeight = 100

  val selfColor = "#008B00"
  val othersColor = "#8B0000"

  object PreWindow {
    val width = 900
    val height = 500
  }

  object RoomWindow {
    val width = 500
    val height = 500
  }

  object FireAction {
    val attack = 1
    val speedUp = 2
    val stopSpeedUp = 3
  }

  val pictureMap = mutable.HashMap[String,String] (
    "background.png" -> "img/background.png",
    "char1-0.png" -> "img/char1-0.png",
    "char1-1.png" -> "img/char1-1.png",
    "char1-2.png" -> "img/char1-2.png",
    "char1-3.png" -> "img/char1-3.png",
    "char2-0.png" -> "img/char2-0.png",
    "char2-1.png" -> "img/char2-1.png",
    "char2-2.png" -> "img/char2-2.png",
    "char2-3.png" -> "img/char2-3.png",
    "char3-0.png" -> "img/char3-0.png",
    "char3-1.png" -> "img/char3-1.png",
    "char3-2.png" -> "img/char3-2.png",
    "char3-3.png" -> "img/char3-3.png",
    "char4-0.png" -> "img/char4-0.png",
    "char4-1.png" -> "img/char4-1.png",
    "char4-2.png" -> "img/char4-2.png",
    "char4-3.png" -> "img/char4-3.png",
    "char5-0.png" -> "img/char5-0.png",
    "char5-1.png" -> "img/char5-1.png",
    "char5-2.png" -> "img/char5-2.png",
    "char5-3.png" -> "img/char5-3.png",
    "food-sheet0-0.png" -> "img/food-sheet0-0.png",
    "food-sheet0-1.png" -> "img/food-sheet0-1.png",
    "food-sheet0-2.png" -> "img/food-sheet0-2.png",
    "food-sheet0-3.png" -> "img/food-sheet0-3.png",
    "food-sheet0-4.png" -> "img/food-sheet0-4.png",
    "food-sheet0-5.png" -> "img/food-sheet0-5.png",
    "food-sheet0-6.png" -> "img/food-sheet0-6.png",
    "food-sheet0-7.png" -> "img/food-sheet0-7.png",
    "food-sheet2.png" -> "img/food-sheet2.png",
    "kill1.png" -> "img/kill1.png",
    "kill2.png" -> "img/kill2.png",
    "kill3.png" -> "img/kill3.png",
    "kill4.png" -> "img/kill4.png",
    "kill5.png" -> "img/kill5.png",
    "kill6.png" -> "img/kill6.png",
    "level-up.png" -> "img/level.png",
    "map.jpg" -> "img/map.png",
    "speedParticles.png" -> "img/speedParticles.png",
    "weapon1.png" -> "img/weapon1.png",
    "weapon2.png" -> "img/weapon2.png",
    "weapon3.png" -> "img/weapon3.png",
    "weapon4.png" -> "img/weapon4.png",
    "weapon5.png" -> "img/weapon5.png",
    "weapon6.png" -> "img/weapon6.png",
    "bar.png" -> "img/bar.png",
    "fillBar.png" -> "img/fillBar.png",
    "hammer.png" -> "img/hammer.png",
    "cursor.png" -> "img/cursor.png",
    "crown.png" -> "img/crown.png",
    "cursor3.png" -> "img/cursor3.png",
    "cursor5.png" -> "img/cursor5.png",
    "star.png" -> "img/star.png"
  )
}
