package org.seekloud.utils

import scala.collection.mutable

/**
  * User: TangYaruo
  * Date: 2019/1/17
  * Time: 16:19
  */
object BwClient {

  val uploadStatistics = mutable.HashMap[String, Double] (
    "MM" -> 0.0,
    "MM_num" -> 0,
    "MouseClickDownLeft" -> 0.0,
    "MouseClickDownLeft_num" -> 0,
    "MouseClickUpRight" -> 0.0,
    "MouseClickUpRight_num" -> 0,
    "MouseClickDownRight" -> 0.0,
    "MouseClickDownRight_num" -> 0,
    "RestartGame" -> 0.0,
    "RestartGame_num" -> 0,
    "PingPackage" -> 0.0,
    "PingPackage_num" -> 0,
    "others" -> 0.0
  )

  val downloadStatistics = mutable.HashMap[String, Double](
    "GridSyncState" -> 0.0,
    "GridSyncState_num" -> 0,
    "YourInfo" -> 0.0,
    "YourInfo_num" -> 0,
    "UserEnterRoom" -> 0.0,
    "UserEnterRoom_num" -> 0,
    "UserLeftRoom" -> 0.0,
    "UserLeftRoom_num" -> 0,
    "BeAttacked" -> 0.0,
    "BeAttacked_num" -> 0,
    "EatFood" -> 0.0,
    "EatFood_num" -> 0,
    "MM" -> 0.0,
    "MM_num" -> 0,
    "MouseClickDownLeft" -> 0.0,
    "MouseClickDownLeft_num" -> 0,
    "MouseClickUpRight" -> 0.0,
    "MouseClickUpRight_num" -> 0,
    "MouseClickDownRight" -> 0.0,
    "MouseClickDownRight_num" -> 0,
    "Ranks" -> 0.0,
    "Ranks_num" -> 0,
    "PingPackage" -> 0.0,
    "PingPackage_num" -> 0,
    "UserMap" -> 0.0,
    "UserMap_num" -> 0,
    "GenerateFood" -> 0.0,
    "GenerateFood_num" -> 0,
    "others" -> 0.0
  )


  def showStatistics: String = {
    val uploadTotal = uploadStatistics.filterNot(_._1.contains("_num")).values.sum
    val downloadTotal = downloadStatistics.filterNot(_._1.contains("_num")).values.sum
    var kbDetail = ""
    downloadStatistics.foreach { s =>
      s._1 match {
        case "MM" => kbDetail = kbDetail + s"${s._1}: ${uploadStatistics(s._1)} kb (${uploadStatistics(s._1 + "_num")}个), ${s._2} kb (${downloadStatistics(s._1 + "_num")}个)\n"
        case "MouseClickDownLeft" => kbDetail = kbDetail + s"${s._1}: ${uploadStatistics(s._1)} kb (${uploadStatistics(s._1 + "_num")}个), ${s._2} kb (${downloadStatistics(s._1 + "_num")}个)\n"
        case "MouseClickUpRight" => kbDetail = kbDetail + s"${s._1}: ${uploadStatistics(s._1)} kb (${uploadStatistics(s._1 + "_num")}个), ${s._2} kb (${downloadStatistics(s._1 + "_num")}个)\n"
        case "MouseClickDownRight" => kbDetail = kbDetail + s"${s._1}: ${uploadStatistics(s._1)} kb (${uploadStatistics(s._1 + "_num")}个), ${s._2} kb (${downloadStatistics(s._1 + "_num")}个)\n"
        case "PingPackage" => kbDetail = kbDetail + s"${s._1}: ${uploadStatistics(s._1)} kb (${uploadStatistics(s._1 + "_num")}个), ${s._2} kb (${downloadStatistics(s._1 + "_num")}个)\n"
        case "others" => kbDetail = kbDetail + s"${s._1}: ${uploadStatistics(s._1)} kb, ${s._2} kb\n"
        case x: String if !x.contains("_num") => kbDetail = kbDetail + s"${s._1}: ${s._2} kb (${downloadStatistics(s._1 + "_num")}个)\n"
        case _ => // do nothing
      }
    }
    kbDetail = kbDetail + s"RestartGame(up): ${uploadStatistics("RestartGame")} kb (${uploadStatistics("RestartGame_num")} 个)\n"

    val detail = "\n**************************带宽统计**************************\n" +
                 s"TOTAL: ${uploadTotal + downloadTotal} kb (up: $uploadTotal kb + down: $downloadTotal kb)\n" + kbDetail +
                 "************************************************************\n"
    downloadStatistics.foreach(d => downloadStatistics.update(d._1, 0))
    uploadStatistics.foreach(d => uploadStatistics.update(d._1, 0))
    detail
  }





}
