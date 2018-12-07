package org.seekloud.thor.view

import com.neo.sk.utils.JavaFxUtil
import javafx.scene.canvas.GraphicsContext
import org.seekloud.thor.App
import org.seekloud.thor.common.Context
import org.seekloud.thor.shared.ptcl.model.{Constants, Point}
import javafx.scene.canvas.GraphicsContext
import javafx.scene.canvas.Canvas
import javafx.scene.{Group, ImageCursor, Scene}
import javafx.scene.paint.Color
import javafx.scene.image.Image
import javafx.scene.text.{Font, FontWeight, TextAlignment}


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

  val group = new Group()
  val scene = new Scene(group)

  def updateSize= {
    canvasWidth = screen.getMaxX.toFloat
    canvasHeight = screen.getMaxY.toFloat
  }

  def getScene = scene
  def getCanvasContext = canvas.getCtx

  group.getChildren.add(canvas.getCanvas)

  def checkScreenSize() = {
    val newCanvasWidth = context.getStageWidth.toFloat
    val newCanvasHeight = if(context.isFullScreen) context.getStageHeight.toFloat else context.getStageHeight.toFloat - 20
    if(canvasWidth != newCanvasWidth || canvasHeight != newCanvasHeight){
      println("the screen size has changed")
      canvasWidth = newCanvasWidth
      canvasHeight = newCanvasHeight
      canvasUnit = newCanvasWidth / Constants.canvasUnitPerLine
      canvasBoundary = Point(canvasWidth, canvasHeight)
      canvas.setHeight(newCanvasHeight)
      canvas.setHeight(newCanvasHeight)
      (canvasBoundary, canvasUnit)
    }else (Point(0,0),0.toFloat)
  }


  def drawGameLoading(): Unit = {
    val ctx = getCanvasContext
    println("loading")
    ctx.setFill("#000000")
    ctx.fillRec(0, 0, canvasBoundary.x, canvasBoundary.y)
    ctx.setFill("rgb(250, 250, 250)")
    ctx.setTextAlign("left")
    ctx.setFont("Helvetica", 36)
    ctx.fillText("请稍等，正在连接服务器", 150, 180)
  }

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

  def drawReplayMsg(m: String): Unit = {
    val ctx = getCanvasContext
    ctx.setFill("#000000")
    ctx.fillRec(0, 0, canvasBoundary.x, canvasBoundary.y)
    ctx.setFill("rgb(250, 250, 250)")
    ctx.setTextAlign("left")
    ctx.setTextBaseLine("top")
    ctx.setFont("Helvetica", 3.6 * canvasUnit)
    ctx.fillText(m, 150, 180)
    println()
  }

  private val logo = drawFrame.createImage("/img/logo.png")
  private val deadBlank = drawFrame.createImage("/img/dead-blank.png")
  private val userName = drawFrame.createImage("/img/user-name.png")
  private val playAgain = drawFrame.createImage("/img/play-again.png")


  abstract class CanvasListener{
    def updateCanvasBoundary()
  }
}
