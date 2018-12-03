package com.neo.sk.utils

import org.apache.commons.codec.digest.DigestUtils

import scala.util.Random

/**
  * User: Taoz
  * Date: 7/8/2015
  * Time: 8:42 PM
  */
object SecureUtil {

  val random = new Random(System.currentTimeMillis())

  val chars = Array(
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
    'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
  )


  final case class PostEnvelope(
    appId: String,
    sn: String,
    timestamp: String,
    nonce: String,
    data: String,
    signature: String
  )

  def checkPostEnvelope(envelope: PostEnvelope, secureKey: String): Boolean = {
    import envelope._
    val params = List(appId, sn, timestamp, nonce, data)
    checkSignature(params, signature, secureKey)
  }


  def genPostEnvelope(appId: String, sn: String, data: String, secureKey: String): PostEnvelope = {
    val params = List(appId, sn, data)
    val (timestamp, nonce, signature) = generateSignatureParameters(params, secureKey)
    PostEnvelope(appId, sn, timestamp, nonce, data, signature)
  }


  def getSecurePassword(password: String, ip: String, timestamp: Long): String = {
    DigestUtils.sha1Hex(DigestUtils.md5Hex(timestamp + password) + ip + timestamp)
  }

  def checkSignature(parameters: List[String], signature: String, secureKey: String): Boolean = {
    val e=generateSignature(parameters, secureKey)
    e == signature
  }

  def generateSignature(parameters: List[String], secureKey: String): String = {
    val strSeq = (secureKey :: parameters).sorted.mkString("")
    //println(s"strSeq: $strSeq")
    DigestUtils.sha1Hex(strSeq)
  }

  def generateSignatureParameters(parameters: List[String], secureKey: String): (String, String, String) = {
    val timestamp = System.currentTimeMillis().toString
    val nonce = nonceStr(6)
    val pList = nonce :: timestamp :: parameters
    val signature = generateSignature(pList, secureKey)
    (timestamp, nonce, signature)
  }

  def nonceStr(length: Int): String = {
    val range = chars.length
    (0 until length).map { _ =>
      chars(random.nextInt(range))
    }.mkString("")
  }


  def checkStringSign(str: String, sign: String, secureKey: String): Boolean = {
    stringSign(str, secureKey) == sign
  }

  def stringSign(str: String, secureKey: String): String = {
    DigestUtils.sha1Hex(secureKey + str)
  }


  def main(args: Array[String]) {
    println(nonceStr(20))

  }


}
