package com.neo.sk.thor.protocol

import com.neo.sk.thor.shared.ptcl.ErrorRsp

/**
  * User: lyh831
  * Date: 2018/3/14
  * Time: 14:58
  */
object CommonErrorCode{
  def internalError(message:String) = ErrorRsp(1000101,s"internal error: $message")

  def noSessionError(message:String="no session") = ErrorRsp(1000202,s"$message")

  def parseJsonError =ErrorRsp(1000103,"parse json error")

  def userAuthError =ErrorRsp(1000104,"your auth is lower than user")

  def adminAuthError=ErrorRsp(1000105,"your auth is lower than admin")

  def signatureError=ErrorRsp(msg= "signature wrong.",errCode = 1000106)

  def operationTimeOut =ErrorRsp(msg= "operation time out.",errCode = 1000107)

  def appIdInvalid =ErrorRsp(msg="appId invalid.",errCode=1000108)
  def notAdmin = ErrorRsp(1000205,"you are not admin")

  def requestIllegal(body:String = "") = ErrorRsp(msg=s"receive illegal request body;$body.",errCode = 1000109)

  def smthWxApiNoHeader =ErrorRsp(1000110,"your request header is not valid")
  def dogwoodWxApiNoHeader =ErrorRsp(1000115,"your request header is not valid")
  def smthWxApiAuthError(msg:String)=ErrorRsp (1000111,s"smth wx api auth error,msg=${msg}")
  def dogwoodWxApiAuthError(msg:String)=ErrorRsp (1000114,s"dogwood wx api auth error,msg=${msg}")

  def requestTimeOut = ErrorRsp(1000003, "request timestamp is too old.")

  def requestAskActorTimeOut = ErrorRsp(1000112, "网络繁忙，请重试")

  def loginAuthError = ErrorRsp(1000113, "this interface auth need login")

  def sessionErrorRsp(msg: String) = ErrorRsp(1001320, msg)

  def checkUserAuthorityErrorRsp(msg: String) = ErrorRsp(1001404, msg)







}
