package org.seekloud.thor.front.utils

import org.scalajs.dom.ext.LocalStorage

/**
  * User: Taoz
  * Date: 3/6/2017
  * Time: 9:55 PM
  */
object LocalStorageUtil {


  import io.circe._
  import io.circe.generic.auto._
  import io.circe.parser._
  import io.circe.syntax._

  val USER_INFO_KEY = "USER_INFO"
  val ROOM_LIST_KEY = "ROOM_LIST"

  private def loadAndParse[T](key: String)(implicit decoder: Decoder[T]): Option[T] = {
    LocalStorage.apply(key).flatMap { str =>
      println(s"load storage [$key]: $str")
      decode[T](str)(decoder) match {
        case Right(v) => Some(v)
        case Left(error) =>
          println(s"decode error: $error")
          None
      }
    }
  }




/*
  def removeUserInfo(): Unit = {
    LocalStorage.remove(USER_INFO_KEY)
  }

  def removeRoomList(): Unit = {
    LocalStorage.remove(ROOM_LIST_KEY)
  }

  def storeUserInfo(userInfo: UserInfo): Unit = {
    LocalStorage.update(USER_INFO_KEY, userInfo.asJson.noSpaces)
  }

  def storeRoomList(rooms: List[RoomInfo]): Unit = {
    val str = rooms.asJson.noSpaces
    println(s"storeRoomList: $str")
    LocalStorage.update(ROOM_LIST_KEY, rooms.asJson.noSpaces)
  }

  def loadUserInfo(): Option[UserInfo] = {
    loadAndParse[UserInfo](USER_INFO_KEY)
  }

  def loadRoomList(): Option[List[RoomInfo]] = {
    loadAndParse[List[RoomInfo]](ROOM_LIST_KEY)
  }
*/


}
