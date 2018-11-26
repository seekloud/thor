package org.seekloud.thor.protocol

/**
  * User: TangYaruo
  * Date: 2018/11/25
  * Time: 16:34
  */
object ReplayProtocol {

  final case class EssfMapKey(
    playerId: String,
    name: String
  )

  final case class EssfMapJoinLeftInfo(
    joinF: Long,
    leftF: Long
  )

  final case class EssfMapInfo(m:List[(EssfMapKey,EssfMapJoinLeftInfo)])




}
