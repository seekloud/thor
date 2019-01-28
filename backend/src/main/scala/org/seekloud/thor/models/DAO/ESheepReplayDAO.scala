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

package org.seekloud.thor.models.DAO

import org.seekloud
import org.seekloud.thor
import org.seekloud.thor.models
import org.seekloud.thor.models.SlickTables
import org.seekloud.utils.DBUtil.db
import slick.jdbc.PostgresProfile.api._
import org.seekloud.thor.models.SlickTables._

import scala.concurrent.Future
/**
  * User: XuSiRan
  * Date: 2018/11/29
  * Time: 11:22
  * 重复的功能
  */
object ESheepReplayDAO {

  def getRecordList(lastId: Long, count: Int): Future[Seq[(thor.models.SlickTables.rGameRecord, Option[thor.models.SlickTables.rUserRecordMap])]] ={
    if(lastId == 0l){
      db.run(tGameRecord.sortBy(_.recordId.desc).take(count).joinLeft(tUserRecordMap).on(_.recordId === _.recordId).result)
    }
    else {
      db.run(tGameRecord.filter(t => t.recordId < lastId).sortBy(_.recordId.desc).take(count).joinLeft(tUserRecordMap).on(_.recordId === _.recordId).result)
    }
  }

  def getRecordListByTime(sTime: Long, eTime: Long, lastId: Long, count: Int): Future[Seq[(seekloud.thor.models.SlickTables.rGameRecord, Option[seekloud.thor.models.SlickTables.rUserRecordMap])]] ={
    if(lastId == 0l){
      db.run(tGameRecord.filter(t => t.startTime >= sTime && t.startTime < eTime).sortBy(_.recordId.desc).take(count).joinLeft(tUserRecordMap).on(_.recordId === _.recordId).result)
    }
    else {
      db.run(tGameRecord.filter(t => t.recordId < lastId && t.startTime >= sTime && t.startTime < eTime).sortBy(_.recordId.desc).take(count).joinLeft(tUserRecordMap).on(_.recordId === _.recordId).result)
    }
  }

  def getRecordListByPlayer(playerId: String, lastId: Long, count: Int): Future[Seq[(_root_.org.seekloud.thor.models.SlickTables.rGameRecord, Option[_root_.org.seekloud.thor.models.SlickTables.rUserRecordMap])]] ={
    if(lastId == 0l){
      db.run(tGameRecord.sortBy(_.recordId.desc).take(count).joinLeft(tUserRecordMap).on(_.recordId === _.recordId).filter(a => a._2.map(_.userId === playerId)).result)
    }
    else {
      db.run(tGameRecord.filter(t => t.recordId < lastId).sortBy(_.recordId.desc).take(count).joinLeft(tUserRecordMap).on(_.recordId === _.recordId).filter(a => a._2.map(_.userId === playerId)).result)
    }
  }

  def getRecordPlayerList(recordId: Long, playerId: String) ={

  }

  def getRecordFrame(recordId: Long, playerId: String) ={

  }
}
