package com.neo.sk.thor.front.thorClient

import com.neo.sk.thor.front.thorClient.draw.{AdventurerClient, FoodClient}
import com.neo.sk.thor.shared.ptcl.config.ThorGameConfig
import com.neo.sk.thor.shared.ptcl.thor.ThorSchemaImpl
import org.scalajs.dom

/**
  * User: TangYaruo
  * Date: 2018/11/12
  * Time: 16:17
  */
class ThorSchemaClientImpl (
                             ctx:dom.CanvasRenderingContext2D,
                             override implicit val config: ThorGameConfig,
                             myId: String,
                             myName: String
                           ) extends ThorSchemaImpl(config, myId, myName)
with AdventurerClient
with FoodClient{

  def drawGame(): Unit ={
    drawAdventurer()
    drawFood()
  }
}
