package com.neo.sk.thor.front.thorClient

import com.neo.sk.thor.front.thorClient.draw.{AdventurerClient, DrawOtherClient, FoodClient}
import com.neo.sk.thor.shared.ptcl.`object`.Adventurer
import com.neo.sk.thor.shared.ptcl.config.ThorGameConfig
import com.neo.sk.thor.shared.ptcl.thor.ThorSchemaImpl
import org.scalajs.dom

import scala.collection.mutable

/**
  * User: TangYaruo
  * Date: 2018/11/12
  * Time: 16:17
  */
case class ThorSchemaClientImpl (
                             protected val ctx:dom.CanvasRenderingContext2D,
                             override implicit val config: ThorGameConfig,
                             myId: String,
                             myName: String
                           ) extends ThorSchemaImpl(config, myId, myName)
with AdventurerClient
with FoodClient
with DrawOtherClient{

//  protected val adventurerAttackAnimationMap: mutable.HashMap[Int, Int] = mutable.HashMap[Int, Int]() //可能存在的挥刀动画 id->动画id

  def drawGame(offSetTime:Long): Unit ={
    if(!waitSyncData){
      adventurerMap.get(myId) match{
        case Some(adventurer) =>
          //TODO 各种环境绘画
          drawAdventurer()
          drawFoodByOffsetTime(offSetTime)
          drawBackground()
        case None =>()
      }
    }
  }
}
