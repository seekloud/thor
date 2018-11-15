package com.neo.sk.thor.core.game

import java.util.concurrent.atomic.AtomicInteger

import com.neo.sk.thor.shared.ptcl.`object`._
import org.slf4j.Logger
import com.neo.sk.thor.shared.ptcl.config.ThorGameConfig
import com.neo.sk.thor.shared.ptcl.model._
import com.neo.sk.thor.shared.ptcl.protocol.ThorGame._
import com.neo.sk.thor.shared.ptcl.thor.ThorSchema

/**
  * User: XuSiRan
  * Date: 2018/11/15
  * Time: 11:37
  */
class ThorSchemaServerImpl (
                             override implicit val config: ThorGameConfig,
                             log: Logger
                             // TODO 参数
                           )extends ThorSchema{

  import scala.language.implicitConversions

  override def debug(msg: String): Unit = log.debug(msg)

  override def info(msg: String): Unit = log.info(msg)

  private val foodIdGenerator = new AtomicInteger(100)

  override protected implicit def adventurerState2Impl(adventurer: AdventurerState): Adventurer = {
    //TODO AdventurerState 转 Adventurer 具体实现
    new AdventurerImpl(config, adventurer)
  }

  //↓↓↓需要重写的函数↓↓↓

  override protected def adventurerEatFoodCallback(adventurer: Adventurer)(food: Food): Unit = {
    val event = EatFood(adventurer.playerId, food.fId, food.level, systemFrame)
    addGameEvent(event)
    //TODO dispatch ?
  }

  override def update(): Unit = super.update()

  //↓↓↓只有后台执行的函数↓↓↓

  private final def gengerateFood(level: Int = 1, position: Point, radius: Float = 2): Unit ={
    //生成食物事件，被后台定时事件调用，前端不产生此事件，食物的属性暂且全部作为参数
    val foodState = FoodState(foodIdGenerator.getAndIncrement(), level, position, radius)
    val event = GenerateFood(systemFrame, foodState)
    addGameEvent(event)
    //TODO dispatch ?
  }

}
