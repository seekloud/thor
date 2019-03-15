package org.seekloud.thor.scene

import javafx.geometry.Rectangle2D
import javafx.scene.canvas.Canvas
import javafx.scene.{Group, Scene}
import javafx.stage.Screen
import org.seekloud.thor.common.StageContext
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
  val allPlayerCanvas: MiddleCanvasInFx = drawFrame.createCanvas(canvasWidth * 2,canvasHeight * 2 + 210)
  val allCanvas: MiddleCanvasInFx = drawFrame.createCanvas(canvasWidth,canvasHeight)
  val selfCanvas: MiddleCanvasInFx = drawFrame.createCanvas(canvasWidth,canvasHeight)
  val mouseCanvas: MiddleCanvasInFx = drawFrame.createCanvas(canvasWidth,canvasHeight)
  val stateCanvas: MiddleCanvasInFx = drawFrame.createCanvas(canvasWidth,canvasHeight)
  protected var canvasUnitPerLine = 100

//  positionCanvas.getCanvas.setId("0")
//  borderCanvas.getCanvas.setId("1")
//  foodCanvas.getCanvas.setId("2")
//  allPlayerCanvas.getCanvas.setId("3")
//  allCanvas.getCanvas.setId("4")
//  selfCanvas.getCanvas.setId("5")
//  mouseCanvas.getCanvas.setId("6")
//  stateCanvas.getCanvas.setId("7")

  positionCanvas.getCanvas.setLayoutY(10)
  positionCanvas.getCanvas.setLayoutX(815)
  borderCanvas.getCanvas.setLayoutY(10)
  borderCanvas.getCanvas.setLayoutX(1220)
  foodCanvas.getCanvas.setLayoutY(215)
  foodCanvas.getCanvas.setLayoutX(815)
  allPlayerCanvas.getCanvas.setLayoutY(10)
  allPlayerCanvas.getCanvas.setLayoutX(10)
  allCanvas.getCanvas.setLayoutY(420)
  allCanvas.getCanvas.setLayoutX(815)
  selfCanvas.getCanvas.setLayoutY(420)
  selfCanvas.getCanvas.setLayoutX(1220)
  mouseCanvas.getCanvas.setLayoutY(415)
  mouseCanvas.getCanvas.setLayoutX(410)
  stateCanvas.getCanvas.setLayoutY(215)
  stateCanvas.getCanvas.setLayoutX(1220)

  val group = new Group()
  val scene = new Scene(group)
  group.getChildren.add(positionCanvas.getCanvas)
  group.getChildren.add(borderCanvas.getCanvas)
  group.getChildren.add(foodCanvas.getCanvas)
  group.getChildren.add(allPlayerCanvas.getCanvas)
  group.getChildren.add(allCanvas.getCanvas)
  group.getChildren.add(selfCanvas.getCanvas)
  group.getChildren.add(mouseCanvas.getCanvas)
  group.getChildren.add(stateCanvas.getCanvas)

  def getScene:Scene = scene
  def positionCtx: MiddleContextInFx = positionCanvas.getCtx
  def borderCtx: MiddleContextInFx = borderCanvas.getCtx
  def foodCtx: MiddleContextInFx = foodCanvas.getCtx
  def allPlayerCtx: MiddleContextInFx = allPlayerCanvas.getCtx
  def allCtx: MiddleContextInFx = allCanvas.getCtx
  def selfCtx: MiddleContextInFx = selfCanvas.getCtx
  def mouseCtx: MiddleContextInFx = mouseCanvas.getCtx
  def stateCtx: MiddleContextInFx = stateCanvas.getCtx

  private val ctxMap: Map[String,MiddleContextInFx] = Map(
    "position"  ->  positionCtx,
    "border"    ->  borderCtx,
    "food"      ->  foodCtx,
    "allPlayer" ->  allPlayerCtx,
    "all"       ->  allCtx,
    "self"      ->  selfCtx,
    "mouse"     ->  mouseCtx,
    "state"     ->  stateCtx)

  def getCtxMap: Map[String,MiddleContextInFx] = ctxMap

//  def handleResize(level: Int, context: StageContext): (Point, Float) = {
//    val width = context.getStageWidth.toFloat
//    val height = context.getStageHeight.toFloat
//    val perLine = 120 + 10 * level
//    if(width != canvasWidth || height != canvasHeight || perLine != canvasUnitPerLine){
//      canvasWidth = width
//      canvasHeight = height
//      canvasUnitPerLine =
//        if(perLine == canvasUnitPerLine) canvasUnitPerLine
//        else if (perLine < canvasUnitPerLine) canvasUnitPerLine - 1
//        else canvasUnitPerLine + 1
//      canvasUnit = canvasWidth / canvasUnitPerLine
//      canvasBoundary = Point(canvasWidth, canvasHeight)
//      canvasBounds = canvasBoundary / canvasUnit
//      canvas.setWidth(canvasWidth)
//      canvas.setHeight(canvasHeight)
//      (canvasBoundary, canvasUnit)
//    } else {
//      (Point(0,0), 0.toFloat)
//    }
//  }


}
