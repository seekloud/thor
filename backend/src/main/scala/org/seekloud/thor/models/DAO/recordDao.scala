package org.seekloud.thor.models.DAO

import org.seekloud.thor.models.SlickTables._
import org.seekloud.utils.DBUtil.db
import slick.jdbc.PostgresProfile.api._
import org.seekloud.thor.Boot.executor

/**
  * User: TangYaruo
  * Date: 2018/11/29
  * Time: 11:23
  */
object recordDao {
  def getRecordById(id:Long)={
    db.run(tGameRecord.filter(_.recordId===id).result.headOption)
  }
  def insertGameRecord(g: rGameRecord) = {
    db.run( tGameRecord.returning(tGameRecord.map(_.recordId)) += g)
  }

  def insertUserRecord(u: rUserRecordMap)={
    db.run(tUserRecordMap += u)
  }

  def insertUserRecordList(list: List[rUserRecordMap])={
    db.run(tUserRecordMap ++= list)
  }

  def updataGameRecord(id:Long, endTime:Long) = {
    db.run(tGameRecord.filter(_.recordId === id).map(_.endTime).update(endTime))
  }

  //选择所有的录像
  def queryAllRec(lastId: Long, count: Int) = {
    if(lastId == 0L){
      val q = for {
        rst <- tGameRecord.sortBy(_.recordId.desc).take(count).joinLeft(tUserRecordMap).on(_.recordId === _.recordId).result
      } yield rst
      db.run(q)
    }else{
      val q = for {
        rst <- tGameRecord.filter(r => r.recordId < lastId).sortBy(_.recordId.desc).take(count).joinLeft(tUserRecordMap).on(_.recordId === _.recordId).result
      } yield rst
      db.run(q)
    }
  }

  //根据时间选择录像
  def queryRecByTime(startTime: Long, endTime: Long, lastId: Long, count: Int) = {
    if(lastId == 0L){
      val q = for {
        rst <- tGameRecord.filter(r => r.startTime >= startTime && r.endTime <= endTime).sortBy(_.recordId.desc).take(count).joinLeft(tUserRecordMap).on(_.recordId === _.recordId).result
      } yield rst
      db.run(q)
    }else{
      val q = for {
        rst <- tGameRecord.filter(r => r.recordId < lastId && r.startTime >= startTime && r.endTime <= endTime).sortBy(_.recordId.desc).take(count).joinLeft(tUserRecordMap).on(_.recordId === _.recordId).result
      } yield rst
      db.run(q)
    }

  }

  //根据用户选择录像
  def queryRecByPlayer(userId: String, lastId: Long, count: Int) = {
    if(lastId == 0L){
      val action = for{
        recordIds <- tUserRecordMap.filter(t => t.userId === userId).sortBy(_.recordId.desc).map(_.recordId).take(count).result
        rst <- tGameRecord.filter(_.recordId.inSet(recordIds)).joinLeft(tUserRecordMap).on(_.recordId === _.recordId).result
      } yield {
        rst
      }
      db.run(action.transactionally)
    }else{
      val action = for{
        recordIds <- tUserRecordMap.filter(t => t.userId === userId && t.recordId < lastId).sortBy(_.recordId.desc).map(_.recordId).take(count).result
        rst <- tGameRecord.filter(_.recordId.inSet(recordIds)).joinLeft(tUserRecordMap).on(_.recordId === _.recordId).result
      } yield {
        rst
      }
      db.run(action.transactionally)
    }

  }

  //根据房间号选择ID
  def queryRecByRoom(roomId:Long, lastId: Long, count: Int) = {
    if(lastId == 0L){
      val q = for {
        rst <- tGameRecord.filter(r => r.roomId === roomId).sortBy(_.recordId.desc).take(count).joinLeft(tUserRecordMap).on(_.recordId === _.recordId).result
      } yield rst
      db.run(q)
    }else{
      val q = for {
        rst <- tGameRecord.filter(r => r.recordId < lastId && r.roomId === roomId).sortBy(_.recordId.desc).take(count).joinLeft(tUserRecordMap).on(_.recordId === _.recordId).result
      } yield rst
      db.run(q)
    }

  }

  //根据录像Id选择录像
  def queryRecById(recordId:Long) = {
    val q = for {
      rst <- tGameRecord.filter(r => r.recordId === recordId).joinLeft(tUserRecordMap).on(_.recordId === _.recordId).result
    } yield rst
    db.run(q)
  }

  def getFilePath(recordId:Long) = {
    val q = tGameRecord.filter(_.recordId === recordId).map(_.filePath).result
    db.run(q)
  }

  //insert code
//  def insertCodeForDownload(time:Long, code:String) = {
//    val insertAc =
//      tCodeForDownload += rCodeForDownload(0, time, code)
//    db.run(insertAc)
//  }
//
//  //select code
//  def selectCodeForDownload(time:Long) = {
//    val q = tCodeForDownload.filter(r => r.deadline > time).map(_.code).result
//    db.run(q)
//  }
//
//  //delete code
//  def deleteCodeForDownload(time:Long) = {
//    val q = tCodeForDownload.filter(r => r.deadline < time).delete
//    db.run(q)
//  }
}
