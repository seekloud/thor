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
  var canvasBoundary4Huge: Point = Point(CanvasWidth,CanvasHeight)
  var canvasUnit: Float = canvasWidth / Constants.canvasUnitPerLine
  var canvasUnit4Huge: Float = CanvasWidth / Constants.canvasUnitPerLine
  var canvasBounds: Point = canvasBoundary / canvasUnit
  var canvasBounds4Huge: Point = canvasBoundary4Huge / canvasUnit4Huge
  val positionCanvas: MiddleCanvasInFx = drawFrame.createCanvas(canvasWidth,canvasHeight)
  val borderCanvas: MiddleCanvasInFx = drawFrame.createCanvas(canvasWidth,canvasHeight)
  val foodCanvas: MiddleCanvasInFx = drawFrame.createCanvas(canvasWidth,canvasHeight)
  val allPlayerCanvas: MiddleCanvasInFx = drawFrame.createCanvas(canvasWidth,canvasHeight)
  val allCanvas: MiddleCanvasInFx = drawFrame.createCanvas(canvasWidth,canvasHeight)
  val selfCanvas: MiddleCanvasInFx = drawFrame.createCanvas(canvasWidth,canvasHeight)
  val mouseCanvas: MiddleCanvasInFx = drawFrame.createCanvas(canvasWidth,canvasHeight)
  val stateCanvas: MiddleCanvasInFx = drawFrame.createCanvas(canvasWidth,canvasHeight)
  val humanCanvas: MiddleCanvasInFx = drawFrame.createCanvas(CanvasWidth,CanvasHeight + 210)
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
  borderCanvas.getCanvas.setLayoutX(1020)
  foodCanvas.getCanvas.setLayoutY(115)
  foodCanvas.getCanvas.setLayoutX(815)
  allPlayerCanvas.getCanvas.setLayoutY(115)
  allPlayerCanvas.getCanvas.setLayoutX(1020)
  allCanvas.getCanvas.setLayoutY(220)
  allCanvas.getCanvas.setLayoutX(815)
  selfCanvas.getCanvas.setLayoutY(220)
  selfCanvas.getCanvas.setLayoutX(1020)
  mouseCanvas.getCanvas.setLayoutY(325)
  mouseCanvas.getCanvas.setLayoutX(815)
  stateCanvas.getCanvas.setLayoutY(325)
  stateCanvas.getCanvas.setLayoutX(1020)
  humanCanvas.getCanvas.setLayoutY(10)
  humanCanvas.getCanvas.setLayoutX(10)

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
  group.getChildren.add(humanCanvas.getCanvas)

  def getScene:Scene = scene
  def positionCtx: MiddleContextInFx = positionCanvas.getCtx
  def borderCtx: MiddleContextInFx = borderCanvas.getCtx
  def foodCtx: MiddleContextInFx = foodCanvas.getCtx
  def allPlayerCtx: MiddleContextInFx = allPlayerCanvas.getCtx
  def allCtx: MiddleContextInFx = allCanvas.getCtx
  def selfCtx: MiddleContextInFx = selfCanvas.getCtx
  def mouseCtx: MiddleContextInFx = mouseCanvas.getCtx
  def stateCtx: MiddleContextInFx = stateCanvas.getCtx
  def humanCtx: MiddleContextInFx = humanCanvas.getCtx

  private val ctxMap: Map[String,MiddleContextInFx] = Map(
    "position"  ->  positionCtx,
    "border"    ->  borderCtx,
    "food"      ->  foodCtx,
    "allPlayer" ->  allPlayerCtx,
    "all"       ->  allCtx,
    "self"      ->  selfCtx,
    "mouse"     ->  mouseCtx,
    "state"     ->  stateCtx,
    "human"     ->  humanCtx)

  def getCtxMap: Map[String,MiddleContextInFx] = ctxMap

  def drawWait(): Unit = {
    val w = CanvasWidth
    val h = CanvasHeight
    humanCtx.save()
    humanCtx.setFill("#000000")
    humanCtx.setTextAlign("center")
    humanCtx.setFont("Helvetica", 35)
    val text = "Waiting for bot to join in!"
    val l = humanCtx.measureText(text)
    humanCtx.fillText(text, (w - l) / 2, h / 3)
    humanCtx.restore()
  }

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
