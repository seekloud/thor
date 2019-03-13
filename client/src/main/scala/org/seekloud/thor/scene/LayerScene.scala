package org.seekloud.thor.scene

import javafx.geometry.Rectangle2D
import javafx.scene.{Group, Scene}
import javafx.stage.Screen
import org.seekloud.thor.shared.ptcl.model.{Constants, Point}
import org.seekloud.thor.utils.middleware.{MiddleCanvasInFx, MiddleContextInFx, MiddleFrameInFx}
import org.seekloud.thor.model.Constants._

/**
  * User: TangYaruo
  * Date: 2019/3/7
  * Time: 10:55
  */
object LayerScene {

  trait LayerSceneListener {

  }

}

class LayerScene {

  import LayerScene._

  var layerSceneListener: LayerSceneListener = _

  val screen: Rectangle2D= Screen.getPrimary.getVisualBounds
  val drawFrame = new MiddleFrameInFx
  protected var canvasWidth: Float = layeredCanvasWidth
  protected var canvasHeight: Float = layeredCanvasHeight
  var canvasBoundary = Point(canvasWidth,canvasHeight)
  var canvasUnit: Float = canvasWidth / Constants.canvasUnitPerLine
  var canvasBounds: Point = canvasBoundary / canvasUnit
  val positionCanvas: MiddleCanvasInFx = drawFrame.createCanvas(canvasWidth,canvasHeight)
  val borderCanvas: MiddleCanvasInFx = drawFrame.createCanvas(canvasWidth,canvasHeight)
  val foodCanvas: MiddleCanvasInFx = drawFrame.createCanvas(canvasWidth,canvasHeight)
  val allPlayerCanvas: MiddleCanvasInFx = drawFrame.createCanvas(canvasWidth,canvasHeight)
  protected var canvasUnitPerLine = 100

  val group = new Group()
  val scene = new Scene(group)
  group.getChildren.add(positionCanvas.getCanvas)
  group.getChildren.add(borderCanvas.getCanvas)
  group.getChildren.add(foodCanvas.getCanvas)
  group.getChildren.add(allPlayerCanvas.getCanvas)
//  group.getChildren.add(positionCanvas.getCanvas)
//  group.getChildren.add(positionCanvas.getCanvas)
//  group.getChildren.add(positionCanvas.getCanvas)
//  group.getChildren.add(positionCanvas.getCanvas)

  def getScene:Scene = scene
  def getCanvasContext: MiddleContextInFx = positionCanvas.getCtx

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
      positionCanvas.setWidth(canvasWidth)
      positionCanvas.setHeight(canvasHeight)
      (canvasBoundary, canvasUnit)
    } else {
      (Point(0,0), 0.toFloat)
    }
  }


}
