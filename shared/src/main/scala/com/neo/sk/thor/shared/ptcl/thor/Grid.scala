package com.neo.sk.thor.shared.ptcl.thor

import java.awt.event.KeyEvent
import java.util.concurrent.atomic.{AtomicInteger, AtomicLong}

import com.neo.sk.thor.shared.ptcl.model._
import com.neo.sk.thor.shared.ptcl.protocol.{WsFrontProtocol, WsProtocol}
import com.neo.sk.thor.shared.ptcl.protocol.WsFrontProtocol.AdventurerAction

import scala.collection.mutable

/**
  * Created by Jingyi on 2018/11/9
  */


case class GridState(
                                   f:Long,
                                   adventurer:List[AdventurerState],
                                   bullet:List[FoodState],
                                   moveAction:List[(Int,List[Int])]
                                 )

trait Grid {


  val boundary : Point

  def debug(msg: String): Unit

  def info(msg: String): Unit

  var currentRank = List.empty[Score]

  var historyRankMap =Map.empty[Int,Score]
  var historyRank = historyRankMap.values.toList.sortBy(_.d).reverse
  var historyRankThreshold =if (historyRank.isEmpty)-1 else historyRank.map(_.d).min
  val historyRankLength =5

  var systemFrame:Long = 0L //系统帧数

  val adventurerMap = mutable.HashMap[Int,Adventurer]()
  val foodMap = mutable.HashMap[Int,Food]() //bulletId -> Bullet





  def addAction(id:Int,adventurerAction:AdventurerAction) = {

  }

  def addActionWithFrame(id: Int, adventurerAction: AdventurerAction, frame: Long) = {

  }

  def removeActionWithFrame(id: Int, adventurerAction: AdventurerAction, frame: Long) = {

  }



   def updateAdventurer():Unit = {

  }

  def updateFood():Unit = {

  }

  def update():Unit ={
    handleAction()
    updateAdventurer() //更新坦克的移动
    updateFood() //更新坦克的子弹
    systemFrame += 1
  }

  def handleAction():Unit = {


  }




  def leftGame(myId:Int):Unit = {

  }


  def getGridState():GridState = {

  }

}
