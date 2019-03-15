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

package org.seekloud.thor.common

import com.typesafe.config.ConfigFactory


/**
  * User: TangYaruo
  * Date: 2019/3/7
  * Time: 11:22
  */
object BotSettings {

  val botConfig = ConfigFactory.parseResources("bot.conf").withFallback(ConfigFactory.load())

  val mainConfig = botConfig.getConfig("bot")

  /*system info*/
  val frameDuration = mainConfig.getInt("frameDuration")

  val isLayer = mainConfig.getBoolean("isLayer")
  val isViewObservation = mainConfig.getBoolean("isViewObservation")
  val isGray = mainConfig.getBoolean("isGray")
  val botServerPort = mainConfig.getInt("botServerPort")


  /*botSecure*/
  val playerId = mainConfig.getString("botSecure.playerId")
  val apiToken = mainConfig.getString("botSecure.apiToken")

  /*botInfo*/
  val botId = mainConfig.getString("botInfo.botId")
  val botKey = mainConfig.getString("botInfo.botKey")

  val layerCanvasW = mainConfig.getInt("layerCanvas.w")
  val layerCanvasH = mainConfig.getInt("layerCanvas.h")

  val viewCanvasW = mainConfig.getInt("viewCanvas.w")
  val viewCanvasH = mainConfig.getInt("viewCanvas.h")


}
