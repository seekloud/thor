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


  final case class TestPswRsp(
     psw: String,
     errCode: Int = 0,
     msg: String = "ok"
   ) extends CommonRsp

}
