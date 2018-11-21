package org.seekloud.thor.shared.ptcl.model

/**
  * User: TangYaruo
  * Date: 2018/11/14
  * Time: 14:34
  */
object Constants {

  object GameState{
    val firstCome = 1
    val play = 2
    val stop = 3
    val loadingPlay = 4
    val relive = 5
    val replayLoading = 6
  }

  object AdventurerLevel {
    val step = 1

    val levelOne = 1
    val levelTwo = 2
    val levelThree = 3
    val levelFour = 4
    val levelFive = 5
    val levelSix = 6
  }

  object SpeedLevel {
    val step = 10
    val speedUpRatio = 2

    val levelOne: Float = 50
    val levelTwo: Float = 40
    val levelThree: Float = 30
    val levelFour: Float = 20
    val levelFive: Float = 10
  }


  object FoodLevel {
    val levelOne = 1
    val levelTwo = 2
    val levelThree = 3
    val levelFour = 4
    val levelFive = 5
  }

  object WeaponConf {
    val sweepScope: Float = 120
    val sweepStep: Float = 2
  }

  object Energy {
    val speedUpStep = 1
  }


  val preExecuteFrameOffset = 2 //预执行2帧



}
