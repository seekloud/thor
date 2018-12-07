package org.seekloud.thor.view

import com.neo.sk.utils.JavaFxUtil
import javafx.scene.canvas.GraphicsContext
import org.seekloud.thor.common.Context
import javafx.scene.canvas.GraphicsContext
import javafx.scene.canvas.Canvas
import javafx.scene.ImageCursor
import javafx.scene.paint.Color
import javafx.scene.text.{Font, FontWeight, TextAlignment}
import org.scalajs.dom
import org.seekloud.thor.App
import org.seekloud.thor.shared.ptcl.model.{Constants, Point}
import org.seekloud.utils.middleware.{MiddleCanvasInFx, MiddleFrameInFx}

/**
  * @author Jingyi
  * @version 创建时间：2018/12/3
  */
class PlayGameView (context: Context){
  import javafx.stage.Screen

  val screen= Screen.getPrimary.getVisualBounds
  val drawFrame = new MiddleFrameInFx
  protected var canvasWidth = screen.getMaxX.toFloat
  protected var canvasHeight = screen.getMaxY.toFloat
  var canvasBoundary = Point(canvasWidth,canvasHeight)
  var canvasUnit = canvasWidth / Constants.canvasUnitPerLine
  var canvasBounds = canvasBoundary / canvasUnit
  val canvas = drawFrame.createCanvas(canvasWidth,canvasHeight)
  //todo  init

  def handleResize = {
    val width = context.getStageWidth.toFloat
    val height = context.getStageHeight.toFloat
    if(width != canvasWidth || height != canvasHeight){
      canvasWidth = width
      canvasHeight = height
      canvasUnit = canvasWidth / Constants.canvasUnitPerLine
      canvasBoundary = Point(canvasWidth, canvasHeight)
      canvasBounds = canvasBoundary / canvasUnit
      canvas.setWidth(canvasWidth)
      canvas.setHeight(canvasHeight)
      (canvasBoundary, canvasUnit)
    } else (Point(0,0), 0.toFloat)
  }



}
