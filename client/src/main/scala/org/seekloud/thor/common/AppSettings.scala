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

import java.io.File
import com.typesafe.config.ConfigFactory

object AppSettings {

  val config = ConfigFactory.parseResources(" product.conf").withFallback(ConfigFactory.load())

  val appConfig = config.getConfig("app")

  val httpInterface = appConfig.getString("http.interface")
  val httpPort = appConfig.getInt("http.port")

  /*thor server*/
  val serverProtocol = appConfig.getString("server.protocol")
  val serverHost = appConfig.getString("server.host")
  val rootPath = appConfig.getString("server.rootPath")
  val baseUrl = serverProtocol + "://" + serverHost

  val gameServerProtocol = appConfig.getString("gameServer.protocol")
  val gameServerDomain = appConfig.getString("gameServer.domain")
  val gameServerHost = appConfig.getString("gameServer.host")


  val testPsw = appConfig.getString("testPsw")
  val version = appConfig.getString("version")


  val dependenceConfig = config.getConfig("dependence")

  /*esheep server*/
  private val eSheepConfig = dependenceConfig.getConfig("esheep.config")
  val esheepGameName = eSheepConfig.getString("gameName")
  val esheepAppId = eSheepConfig.getLong("appId")
  val esheepSecureKey = eSheepConfig.getString("gsKey")
  val esheepProtocol = eSheepConfig.getString("protocol")
  val esheepHost = eSheepConfig.getString("host")
  val esheepPort = eSheepConfig.getInt("port")
  val esheepDomain = eSheepConfig.getString("domain")
  val esheepUrl = eSheepConfig.getString("url")

}
