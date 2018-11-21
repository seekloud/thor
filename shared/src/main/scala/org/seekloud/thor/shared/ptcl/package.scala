package org.seekloud.thor.shared

/**
  * User: Taoz
  * Date: 5/30/2017
  * Time: 10:37 AM
  */

package object ptcl {



//  trait Response{
//    val errCode: Int
//    val msg: String
//  }
  trait CommonRsp {
    val errCode: Int
    val msg: String
  }

  final case class ErrorRsp(
    errCode: Int,
    msg: String
  ) extends CommonRsp

  final case class SuccessRsp(
    errCode: Int = 0,
    msg: String = "ok"
  ) extends CommonRsp

  final case class ComRsp(
                               errCode: Int = 0,
                               msg: String = "ok"
                             ) extends CommonRsp




}
