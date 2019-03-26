package org.seekloud.thor.scene

import javafx.beans.property.{ObjectProperty, SimpleObjectProperty, SimpleStringProperty, StringProperty}
import javafx.collections.{FXCollections, ObservableList}
import javafx.event.EventHandler
import javafx.geometry.{Insets, Pos}
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.control._
import javafx.scene.effect.DropShadow
import javafx.scene.image.ImageView
import javafx.scene.input.MouseEvent
import javafx.scene.layout.{BorderPane, HBox, VBox}
import javafx.scene.text.Font
import javafx.scene.{Group, Scene}
import org.seekloud.thor.ClientBoot
import org.seekloud.thor.model.Constants
import org.seekloud.thor.protocol.ThorClientProtocol.ClientUserInfo
import org.seekloud.thor.utils.{TimeUtil, WarningDialog}

import scala.collection.mutable


/**
  * User: TangYaruo
  * Date: 2019/3/9
  * Time: 14:48
  */

object RoomScene {

  case class RoomInfo(
    roomId: StringProperty,
    roomName: StringProperty,
    playerNum: StringProperty,
    createTime: StringProperty,
    isLocked: ObjectProperty[ImageView]
  ) {
    def getRoomId: String = roomId.get()

    def setRoomId(id: String): Unit = roomId.set(id)

    def getRoomName: String = roomId.get()

    def setRoomName(id: String): Unit = roomId.set(id)

    def getPlayerNum: String = playerNum.get()

    def setPlayerNum(num: String): Unit = playerNum.set(num)

    def getIsLocked: ImageView = isLocked.get()

    def setIsLocked(img: ImageView): Unit = isLocked.set(img)

  }

  trait RoomSceneListener {

    def confirmRoom(roomId: Int, hasPwd: Boolean)

    def gotoCreateRoomScene()

    def refreshRoomList()

    def backToPrevious()

  }

}


class RoomScene(userInfo: ClientUserInfo) {


  import RoomScene._

  val width: Int = Constants.RoomWindow.width
  val height: Int = Constants.RoomWindow.height
  val group = new Group
  private val scene = new Scene(group, width, height)

  scene.getStylesheets.add(
    this.getClass.getClassLoader.getResource("css/scene.css").toExternalForm
  )

  private val borderPane = new BorderPane()
  private val hBox = new HBox(20)
  private val buttonVBox = new VBox(20)
  private val tableVBox = new VBox(10)
  tableVBox.setPadding(new Insets(15, 5, 20, 5))


  private val roomMap = mutable.HashMap.empty[Int, (Int, Boolean)] //roomId -> (playerNum, isLocked)

  /*room list*/
  private val observableList: ObservableList[RoomInfo] = FXCollections.observableArrayList()
  private val roomTable = new TableView[RoomInfo]()

  val nameLabel = new Label(s"Your Name: ${userInfo.name}")
  nameLabel.setFont(Font.font(17))

  val roomIdCol = new TableColumn[RoomInfo, String]("room_id")
  roomIdCol.setMinWidth(80)
  roomIdCol.setCellValueFactory(new PropertyValueFactory[RoomInfo, String]("roomId"))
  val playerNumCol = new TableColumn[RoomInfo, String]("player_num")
  playerNumCol.setMinWidth(100)
  playerNumCol.setCellValueFactory(new PropertyValueFactory[RoomInfo, String]("playerNum"))
  val isLocked = new TableColumn[RoomInfo, ImageView]("need_psw")
  isLocked.setMinWidth(80)
  isLocked.setCellValueFactory(new PropertyValueFactory[RoomInfo, ImageView]("isLocked"))

  roomTable.setItems(observableList)
  roomTable.getColumns.addAll(roomIdCol, playerNumCol, isLocked)

  /*buttons*/
  private val confirmBtn = new Button("Enter")
  confirmBtn.getStyleClass.add("room-choice")
  confirmBtn.setStyle("-fx-base: #67B567;")
  private val roomBtn = new Button("Create")
  roomBtn.getStyleClass.add("room-choice")
  roomBtn.setStyle("-fx-base: #307CF0;")
  private val refreshBtn = new Button("Refresh")
  refreshBtn.getStyleClass.add("room-choice")
  refreshBtn.setStyle("-fx-base: #E6AF5F;")
  private val backBtn = new Button("Back")
  backBtn.getStyleClass.add("room-choice")
  backBtn.setStyle("-fx-base: #CA5C54;")

  val shadow = new DropShadow()

  confirmBtn.addEventHandler(MouseEvent.MOUSE_ENTERED, (_: MouseEvent) => {
    confirmBtn.setEffect(shadow)
  })

  confirmBtn.addEventHandler(MouseEvent.MOUSE_EXITED, (_: MouseEvent) => {
    confirmBtn.setEffect(null)
  })

  roomBtn.addEventHandler(MouseEvent.MOUSE_ENTERED, (_: MouseEvent) => {
    roomBtn.setEffect(shadow)
  })

  roomBtn.addEventHandler(MouseEvent.MOUSE_EXITED, (_: MouseEvent) => {
    roomBtn.setEffect(null)
  })

  refreshBtn.addEventHandler(MouseEvent.MOUSE_ENTERED, (_: MouseEvent) => {
    refreshBtn.setEffect(shadow)
  })

  refreshBtn.addEventHandler(MouseEvent.MOUSE_EXITED, (_: MouseEvent) => {
    refreshBtn.setEffect(null)
  })

  backBtn.addEventHandler(MouseEvent.MOUSE_ENTERED, (_: MouseEvent) => {
    backBtn.setEffect(shadow)
  })

  backBtn.addEventHandler(MouseEvent.MOUSE_EXITED, (_: MouseEvent) => {
    backBtn.setEffect(null)
  })

  /*layout*/
  tableVBox.getChildren.addAll(nameLabel, roomTable)
  buttonVBox.getChildren.addAll(confirmBtn, roomBtn, refreshBtn, backBtn)
  buttonVBox.setAlignment(Pos.CENTER)
  hBox.getChildren.addAll(tableVBox, buttonVBox)
  hBox.setAlignment(Pos.CENTER)
  borderPane.prefWidthProperty().bind(scene.widthProperty())
  borderPane.setCenter(hBox)
  group.getChildren.add(borderPane)


  /**
    * update func
    *
    * @param roomList {roomId}-{roomName}-{playerNum}-{isLocked} (isLocked: 0-false, 1-true)
    **/
  def updateRoomList(roomList: List[String]): Unit = {
    this.roomMap.clear()
    observableList.clear()

    roomList.sortBy(_.split("-").head.toInt).map { r =>
      val roomInfos = r.split("-")
      val roomId = roomInfos(0).toInt
      val roomName = roomInfos(1).toInt
      val playerNum = roomInfos(2).toInt
      val createTime = TimeUtil.TimeStamp2Date(roomInfos(3).toLong)
      val hasPsw = if (roomInfos(4).toInt == 0) false else true
      val img = if (hasPsw) {
        new ImageView("img/lock.png")
      } else {
        new ImageView("img/unlock.png")
      }
      img.setFitWidth(20)
      img.setFitHeight(20)
      roomMap.put(roomId, (playerNum, hasPsw))
      RoomInfo(
        new SimpleStringProperty(roomId.toString),
        new SimpleStringProperty(roomName.toString),
        new SimpleStringProperty(playerNum.toString),
        new SimpleStringProperty(createTime.toString),
        new SimpleObjectProperty[ImageView](img)
      )
    } foreach observableList.add

  }


  def getScene: Scene = this.scene

  var listener: RoomSceneListener = _

  confirmBtn.setOnAction { _ =>
    val selectedInfo = roomTable.getSelectionModel.selectedItemProperty().get()
    val roomId = try {
      selectedInfo.roomId.get().toInt
    } catch {
      case _: Exception =>
        ClientBoot.addToPlatform {
          WarningDialog.initWarningDialog("请选择要加入的房间！")
        }
        -1
    }
    if (roomId != -1) listener.confirmRoom(roomId, roomMap(roomId)._2)
  }
  roomBtn.setOnAction(_ => listener.gotoCreateRoomScene())
  refreshBtn.setOnAction(_ => listener.refreshRoomList())
  backBtn.setOnAction(_ => listener.backToPrevious())

  def setListener(listener: RoomSceneListener): Unit = {
    this.listener = listener
  }


}
