package org.seekloud.thor.scene

import javafx.scene.{Group, Scene}
import javafx.scene.input.KeyCode
import javafx.stage.Screen
import org.seekloud.thor.shared.ptcl.model.{Constants, Point}
import org.seekloud.thor.utils.middleware.{MiddleContextInFx, MiddleFrameInFx}

/**
  * User: TangYaruo
  * Date: 2019/3/7
  * Time: 10:55
  */

object GameScene {

  trait GameSceneListener {
    def onKeyPressed(e: KeyCode): Unit
  }
}


class GameScene() {

  import GameScene._

  var gameSceneListener: GameSceneListener = _

  val screen= Screen.getPrimary.getVisualBounds
  val drawFrame = new MiddleFrameInFx
  protected var canvasWidth = screen.getMaxX.toFloat
  protected var canvasHeight = screen.getMaxY.toFloat
  var canvasBoundary = Point(canvasWidth,canvasHeight)
  var canvasUnit = canvasWidth / Constants.canvasUnitPerLine
  var canvasBounds = canvasBoundary / canvasUnit
  val canvas = drawFrame.createCanvas(canvasWidth,canvasHeight)
  protected var canvasUnitPerLine = 100

  val group = new Group()
  val scene = new Scene(group)
  group.getChildren.add(canvas.getCanvas)

  def getScene:Scene = scene
  def getCanvasContext: MiddleContextInFx = canvas.getCtx

  def handleResize(level: Int): (Point, Float) = {
    val width = screen.getWidth.toFloat
    val height = screen.getHeight.toFloat
    val perLine = 120 + 10 * level
    if(width != canvasWidth || height != canvasHeight || perLine != canvasUnitPerLine){
      canvasWidth = width
      canvasHeight = height
      canvasUnitPerLine =
        if(perLine == canvasUnitPerLine) canvasUnitPerLine
        else if (perLine < canvasUnitPerLine) canvasUnitPerLine - 1
        else canvasUnitPerLine + 1
      canvasUnit = canvasWidth / canvasUnitPerLine
      canvasBoundary = Point(canvasWidth, canvasHeight)
      canvasBounds = canvasBoundary / canvasUnit
      canvas.setWidth(canvasWidth)
      canvas.setHeight(canvasHeight)
      (canvasBoundary, canvasUnit)
    } else {
      (Point(0,0), 0.toFloat)
    }
  }


}
