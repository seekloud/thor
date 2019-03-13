package org.seekloud.thor.scene

import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.control.{Button, Label, PasswordField, TextField}
import javafx.scene.effect.DropShadow
import javafx.scene.image.ImageView
import javafx.scene.input.MouseEvent
import javafx.scene.layout.GridPane
import javafx.scene.{Group, Scene}
import org.seekloud.thor.model.Constants
import org.seekloud.thor.scene.ModeScene.ModeSceneListener

/**
  * User: TangYaruo
  * Date: 2019/3/13
  * Time: 16:25
  */

object BotScene {

  trait BotSceneListener{

    def confirm(botId: String, botKey: String)

    def backToPrevious()

  }

}

class BotScene {

  import BotScene._

  val width: Int = Constants.PreWindow.width
  val height: Int = Constants.PreWindow.height
  val group = new Group
  private val scene = new Scene(group, width, height)

  scene.getStylesheets.add(
    this.getClass.getClassLoader.getResource("css/scene.css").toExternalForm
  )

  val img = new ImageView("img/modeBg.jpg")
  img.setFitWidth(width)
  img.setFitHeight(height)
  group.getChildren.add(img)

  /*input*/
  val idLabel = new Label("bot-id:")
  val keyLabel = new Label("bot-key:")
  val botId = new TextField()
  val botKey = new PasswordField()

  idLabel.getStyleClass.add("bot-label")
  keyLabel.getStyleClass.add("bot-label")

  /*button*/
  val confirm = new Button("Confirm")
  confirm.setStyle("#67B567")
  confirm.getStyleClass.add("room-choice")
  val backToPrevious = new Button("Back")
  backToPrevious.setStyle("#CA5C54")
  backToPrevious.getStyleClass.add("room-choice")

  confirm.setLayoutX(250)
  confirm.setLayoutY(230)
  backToPrevious.setLayoutX(350)
  backToPrevious.setLayoutY(230)


  val shadow = new DropShadow()

  confirm.addEventHandler(MouseEvent.MOUSE_ENTERED, (event: MouseEvent) => {
    confirm.setEffect(shadow)
  })

  confirm.addEventHandler(MouseEvent.MOUSE_EXITED, (event: MouseEvent) => {
    confirm.setEffect(null)
  })

  backToPrevious.addEventHandler(MouseEvent.MOUSE_ENTERED, (event: MouseEvent) => {
    backToPrevious.setEffect(shadow)
  })

  backToPrevious.addEventHandler(MouseEvent.MOUSE_EXITED, (event: MouseEvent) => {
    backToPrevious.setEffect(null)
  })


  val grid = new GridPane()
  grid.setHgap(10)
  grid.setVgap(10)
  grid.setPadding(new Insets(140,50,100,120))
  grid.add(idLabel, 0 ,0)
  grid.add(botId, 1 ,0)
  grid.add(keyLabel, 0 ,1)
  grid.add(botKey, 1 ,1)
  grid.setLayoutX(50)
  grid.setLayoutY(10)

  group.getChildren.addAll(grid, confirm, backToPrevious)

  def getScene: Scene = this.scene

  var listener: BotSceneListener = _

  confirm.setOnAction(_ => listener.confirm(botId.getText, botKey.getText()))
  backToPrevious.setOnAction(_ => listener.backToPrevious())

  def setListener(listener: BotSceneListener): Unit = {
    this.listener = listener
  }










}
