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

package org.seekloud.thor.models

// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object SlickTables extends {
  val profile = slick.jdbc.PostgresProfile
} with SlickTables

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait SlickTables {
  val profile: slick.jdbc.JdbcProfile
  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** DDL for all tables. Call .create to execute. */
  lazy val schema: profile.SchemaDescription = tGameRecord.schema ++ tUserRecordMap.schema
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema

  /** Entity class storing rows of table tGameRecord
   *  @param recordId Database column record_id SqlType(bigserial), AutoInc, PrimaryKey
   *  @param roomId Database column room_id SqlType(int8)
   *  @param startTime Database column start_time SqlType(int8)
   *  @param endTime Database column end_time SqlType(int8)
   *  @param filePath Database column file_path SqlType(text) */
  case class rGameRecord(recordId: Long, roomId: Long, startTime: Long, endTime: Long, filePath: String)
  /** GetResult implicit for fetching rGameRecord objects using plain SQL queries */
  implicit def GetResultrGameRecord(implicit e0: GR[Long], e1: GR[String]): GR[rGameRecord] = GR{
    prs => import prs._
    rGameRecord.tupled((<<[Long], <<[Long], <<[Long], <<[Long], <<[String]))
  }
  /** Table description of table game_record. Objects of this class serve as prototypes for rows in queries. */
  class tGameRecord(_tableTag: Tag) extends profile.api.Table[rGameRecord](_tableTag, "game_record") {
    def * = (recordId, roomId, startTime, endTime, filePath) <> (rGameRecord.tupled, rGameRecord.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(recordId), Rep.Some(roomId), Rep.Some(startTime), Rep.Some(endTime), Rep.Some(filePath)).shaped.<>({r=>import r._; _1.map(_=> rGameRecord.tupled((_1.get, _2.get, _3.get, _4.get, _5.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column record_id SqlType(bigserial), AutoInc, PrimaryKey */
    val recordId: Rep[Long] = column[Long]("record_id", O.AutoInc, O.PrimaryKey)
    /** Database column room_id SqlType(int8) */
    val roomId: Rep[Long] = column[Long]("room_id")
    /** Database column start_time SqlType(int8) */
    val startTime: Rep[Long] = column[Long]("start_time")
    /** Database column end_time SqlType(int8) */
    val endTime: Rep[Long] = column[Long]("end_time")
    /** Database column file_path SqlType(text) */
    val filePath: Rep[String] = column[String]("file_path")
  }
  /** Collection-like TableQuery object for table tGameRecord */
  lazy val tGameRecord = new TableQuery(tag => new tGameRecord(tag))

  /** Entity class storing rows of table tUserRecordMap
   *  @param userId Database column user_id SqlType(varchar), Length(32,true)
   *  @param recordId Database column record_id SqlType(int8)
   *  @param roomId Database column room_id SqlType(int8)
   *  @param userNickname Database column user_nickname SqlType(text) */
  case class rUserRecordMap(userId: String, recordId: Long, roomId: Long, userNickname: String)
  /** GetResult implicit for fetching rUserRecordMap objects using plain SQL queries */
  implicit def GetResultrUserRecordMap(implicit e0: GR[String], e1: GR[Long]): GR[rUserRecordMap] = GR{
    prs => import prs._
    rUserRecordMap.tupled((<<[String], <<[Long], <<[Long], <<[String]))
  }
  /** Table description of table user_record_map. Objects of this class serve as prototypes for rows in queries. */
  class tUserRecordMap(_tableTag: Tag) extends profile.api.Table[rUserRecordMap](_tableTag, "user_record_map") {
    def * = (userId, recordId, roomId, userNickname) <> (rUserRecordMap.tupled, rUserRecordMap.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(userId), Rep.Some(recordId), Rep.Some(roomId), Rep.Some(userNickname)).shaped.<>({r=>import r._; _1.map(_=> rUserRecordMap.tupled((_1.get, _2.get, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column user_id SqlType(varchar), Length(32,true) */
    val userId: Rep[String] = column[String]("user_id", O.Length(32,varying=true))
    /** Database column record_id SqlType(int8) */
    val recordId: Rep[Long] = column[Long]("record_id")
    /** Database column room_id SqlType(int8) */
    val roomId: Rep[Long] = column[Long]("room_id")
    /** Database column user_nickname SqlType(text) */
    val userNickname: Rep[String] = column[String]("user_nickname")
  }
  /** Collection-like TableQuery object for table tUserRecordMap */
  lazy val tUserRecordMap = new TableQuery(tag => new tUserRecordMap(tag))
}
