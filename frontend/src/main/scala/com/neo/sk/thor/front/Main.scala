package com.neo.sk.thor.front

import com.neo.sk.thor.front.pages.MainPage

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

/**
  * User: Taoz
  * Date: 6/3/2017
  * Time: 1:03 PM
  */
@JSExportTopLevel("front.Main")
object Main {


  @JSExport
  def run(): Unit = {
    MainPage.show()
  }



}
