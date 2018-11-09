package com.neo.sk.thor.front.common

/**
  * User: Taoz
  * Date: 2/24/2017
  * Time: 10:59 AM
  */
object Routes {


  val base = "/thor"

  def wsJoinGameUrl(name:String) = base + s"/game/join?name=${name}"










}
