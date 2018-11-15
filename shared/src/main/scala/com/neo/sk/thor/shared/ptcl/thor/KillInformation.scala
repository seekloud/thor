package com.neo.sk.thor.shared.ptcl.thor

/**
  * User: TangYaruo
  * Date: 2018/11/15
  * Time: 11:58
  */
trait KillInformation { this: ThorSchema =>
  private var killInfoList = List[(String,String,Long)]() //killerName,killedName, startTimer
  private val maxDisplaySize = 5
  private val displayDuration = 3 //s

  private val displayFrameNum:Long = displayDuration * 1000 / this.config.frameDuration

  protected def addKillInfo(killerName:String,killedName:String) = {
    println(s"----------,$killedName,$killerName,${this.systemFrame}")
    killInfoList = (killerName,killedName,this.systemFrame) :: killInfoList
  }



  protected def updateKillInformation():Unit = {
    killInfoList = killInfoList.filterNot(_._3 + displayFrameNum < this.systemFrame)
  }

  protected def getDisplayKillInfo():List[(String,String,Long)] = {
    val curDisplayNum = math.min(maxDisplaySize,killInfoList.size)
    killInfoList.take(curDisplayNum)
  }

  protected def removeKillInfoByRollback(frame:Long) = {
    killInfoList = killInfoList.filterNot(_._3 >= frame)
  }

}
