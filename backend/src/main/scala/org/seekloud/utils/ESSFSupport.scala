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

package org.seekloud.utils

import java.io.File

import org.seekloud.byteobject.{MiddleBuffer, MiddleBufferInJvm}
import org.seekloud.essf.io.{FrameData, FrameInputStream, FrameOutputStream}
import org.seekloud.thor.common.AppSettings
import org.seekloud.thor.protocol.ReplayProtocol.{EssfMapInfo, EssfMapJoinLeftInfo, EssfMapKey}
import org.seekloud.thor.shared.ptcl.protocol.ThorGame
import org.seekloud.thor.shared.ptcl.protocol.ThorGame.GameInformation
import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.concurrent.Future

/**
  * User: TangYaruo
  * Date: 2018/11/25
  * Time: 16:25
  *
  * copied from tank
  * 本部分实现thor支持ESSF存储文件IO接口
  */
object ESSFSupport {

  import org.seekloud.byteobject.ByteObject._

  private final val log = LoggerFactory.getLogger(this.getClass)

  /**
    * 存储
    *
    * @author hongruying on 2018/8/14
    **/
  def initFileRecorder(fileName: String, index: Int, gameInformation: GameInformation, initStateOpt: Option[ThorGame.GameSnapshot] = None)
    (implicit middleBuffer: MiddleBufferInJvm): FrameOutputStream = {
    val dir = new File(AppSettings.gameDataDirectoryPath)
    if (!dir.exists()) {
      dir.mkdir()
    }
    val file = AppSettings.gameDataDirectoryPath + fileName + s"_$index"
    val name = "tank"
    val version = "0.1"
    val gameInformationBytes = gameInformation.fillMiddleBuffer(middleBuffer).result()
    val initStateBytes = initStateOpt.map {
      case t: ThorGame.GameSnapshot =>
        t.fillMiddleBuffer(middleBuffer).result()
    }.getOrElse(Array[Byte]())
    val recorder = new FrameOutputStream(file)
    recorder.init(name, version, gameInformationBytes, initStateBytes)
    log.debug(s" init success")
    recorder
  }

  def initFileReader(fileName: String) = {
    val input = new FrameInputStream(fileName)
    input
  }

  /** 解码 */
  def metaDataDecode(a: Array[Byte]) = {
    val buffer = new MiddleBufferInJvm(a)
    bytesDecode[GameInformation](buffer)
  }

  def initStateDecode(a: Array[Byte]) = {
    val buffer = new MiddleBufferInJvm(a)
    bytesDecode[ThorGame.GameSnapshot](buffer)
  }

  def userMapDecode(a: Array[Byte]) = {
    val buffer = new MiddleBufferInJvm(a)
    bytesDecode[EssfMapInfo](buffer)
  }

  def userMapEncode(u: mutable.HashMap[EssfMapKey, EssfMapJoinLeftInfo])(implicit middleBuffer: MiddleBufferInJvm) = {
    EssfMapInfo(u.toList).fillMiddleBuffer(middleBuffer).result()
  }


  /** 用于后端先解码数据然后再进行编码传输 */
  def replayEventDecode(a: Array[Byte]): ThorGame.WsMsgServer = {
    if (a.length > 0) {
      val buffer = new MiddleBufferInJvm(a)
      bytesDecode[List[ThorGame.WsMsgServer]](buffer) match {
        case Right(r) =>
          ThorGame.EventData(r)
        case Left(e) =>
          ThorGame.DecodeError()
      }
    } else {
      ThorGame.DecodeError()
    }
  }

  def replayStateDecode(a: Array[Byte]): ThorGame.WsMsgServer = {
    val buffer = new MiddleBufferInJvm(a)
    bytesDecode[ThorGame.GameSnapshot](buffer) match {
      case Right(r) =>
        ThorGame.GridSyncState(r.asInstanceOf[ThorGame.ThorSnapshot].state)
      case Left(e) =>
        ThorGame.DecodeError()
    }
  }


  def readData(input: FrameInputStream, i: Int = 0) = {
    val info = input.init()
    val a = metaDataDecode(info.simulatorMetadata)
    println(s"all frame=${info.frameCount}")
    while (input.hasMoreFrame) {
      input.readFrame() match {
        case Some(FrameData(idx, ev, stOp)) =>
          val event = replayEventDecode(ev)
          stOp.foreach { r =>
            replayStateDecode(r)
          }
          val len = event match {
            case data: ThorGame.EventData =>
              data.list.length
            case _ => 0
          }

          println(s"frame=${input.getFramePosition}, event=$len")
        case None =>
          println("get to the end, no more frame.")
      }
    }

    println(s"finish=$i")
  }

}
