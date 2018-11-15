package com.neo.sk.thor.front.thorClient.draw

import com.neo.sk.thor.front.common.Routes
import com.neo.sk.thor.front.thorClient.ThorSchemaClientImpl
import com.neo.sk.thor.shared.ptcl.`object`.Adventurer
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLElement
import org.scalajs.dom.html
import com.neo.sk.thor.shared.ptcl.model.Point

import scala.collection.mutable


/**
  * Created by Jingyi on 2018/11/9

  */

trait AdventurerClient { this: ThorSchemaClientImpl =>

  private  val mapImg = dom.document.createElement("img").asInstanceOf[html.Image]
  mapImg.setAttribute("src", s"${Routes.base}/static/img/金牌.png")

  def drawAdventurer(): Unit ={

  }

  def drawAdventurerByOffsetTime(offset:Point,offsetTime:Long): Unit ={



  }

  def drawBackground():Unit = {
    val pat = ctx.createPattern(mapImg,"repeat")
    ctx.fillStyle = pat
    ctx.fillRect(0,0,boundary.x,boundary.y)



  }
}






