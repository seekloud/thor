package com.neo.sk.utils

import org.seekloud.thor.common.Constants


/**
  * @author Jingyi
  * @version 创建时间：2018/12/3
  */

object JavaFxUtil {

  def getCanvasUnit(canvasWidth: Float): Int = (canvasWidth / Constants.WindowView.x).toInt


}
