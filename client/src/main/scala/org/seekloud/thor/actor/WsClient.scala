package org.seekloud.thor.actor

import akka.Done
import akka.actor.typed._
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, StashBuffer, TimerScheduler}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ws.{BinaryMessage, WebSocketRequest}
import akka.stream.OverflowStrategy
import akka.stream.typed.scaladsl.ActorSource
import akka.util.{ByteString, ByteStringBuilder}
import akka.http.scaladsl.model.ws._
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.stream.typed.scaladsl.{ActorSink, ActorSource}
import akka.actor.typed.scaladsl.adapter._
import org.seekloud.byteobject.ByteObject._
import org.seekloud.byteobject.MiddleBufferInJvm
import org.seekloud.thor.ClientBoot
import org.seekloud.thor.common.{AppSettings, BotSettings, Routes, StageContext}
import org.seekloud.thor.controller.{BotController, GameController, LoginController, RoomController}
import org.seekloud.thor.shared.ptcl.protocol.ThorGame._
import org.seekloud.thor.ClientBoot.{executor, materializer, system}
import org.seekloud.thor.bot.BotClient
import org.seekloud.thor.protocol.BotProtocol.EnterRoomRsp
import org.seekloud.thor.protocol.{ESheepProtocol, ThorClientProtocol}
import org.seekloud.thor.protocol.ESheepProtocol.{HeartBeat, Ws4AgentRsp}
import org.seekloud.thor.protocol.ThorClientProtocol.ClientUserInfo
import org.seekloud.thor.scene.{GameScene, LayerScene}
import org.seekloud.thor.scene.{BotScene, GameScene}
import org.seekloud.thor.shared.ptcl.protocol.ThorGame
import org.seekloud.thor.utils.{EsheepClient, WarningDialog}
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import scala.concurrent.duration._


/**
  * User: TangYaruo
  * Date: 2019/3/7
  * Time: 11:06
  */
object WsClient {

  private val log = LoggerFactory.getLogger(this.getClass)

  sealed trait WsCommand

  final case class GetLoginController(loginController: LoginController) extends WsCommand

  final case class GetRoomController(roomController: RoomController) extends WsCommand

  final case class EstablishConnection2Es(wsUrl: String) extends WsCommand

  final case class GetLoginInfo(playerId: String, name: String, token: String, tokenExistTime: Int) extends WsCommand

  final case class GetSender(stream: ActorRef[ThorGame.WsMsgFrontSource]) extends WsCommand

  final case class StartGame(roomId: Long, pwd: Option[String] = None) extends WsCommand

  final case object TimerKey4StartGame

  final case class CreateRoom(psw: Option[String]) extends WsCommand

  final case object TimerKey4CreateGame

  final case object JoinRoomSuccess extends WsCommand

  final case class JoinRoomFail(error: String) extends WsCommand

  final case class CreateRoomRsp(roomId: Long) extends WsCommand

  final case class DispatchMsg(msg: ThorGame.WsMsgFront) extends WsCommand

  final case class PlayerInfo(playerId: String, name: String) extends WsCommand

  final case class BotLogin(botId: String, botKey: String, botFrame: Int) extends WsCommand

  final case object Stop extends WsCommand

  case class ClientTest(roomId:Long) extends WsCommand

  case class GetObservationTest() extends WsCommand

  case class ActionSpaceTest() extends WsCommand

  case class ActionTest() extends WsCommand

  case class LeaveRoomTest() extends WsCommand

  case class SystemInfoTest() extends WsCommand

  case object TimerKeyForTest

  case object LeaveRoomKey

  val host = "127.0.0.1"
  val port = 5321
  val playerId = "test"
  val apiToken = "test"

  val client = new BotClient(host, port, playerId, apiToken)

  private[this] def switchBehavior(ctx: ActorContext[WsCommand],
    behaviorName: String,
    behavior: Behavior[WsCommand])
    (implicit stashBuffer: StashBuffer[WsCommand]) = {
    log.debug(s"${ctx.self.path} becomes $behaviorName behavior.")
    stashBuffer.unstashAll(ctx, behavior)
  }

  def create(stageContext: StageContext): Behavior[WsCommand] =
    Behaviors.setup[WsCommand] { ctx =>
      Behaviors.withTimers[WsCommand] { implicit timer =>
        val gameMsgReceiver: ActorRef[ThorGame.WsMsgServer] = system.spawn(GameMsgReceiver.create(ctx.self), "gameMessageReceiver")
        working(gameMsgReceiver, gameMsgSender = null, None, None, None, None, stageContext)
      }
    }


  /**
    * @param gameMsgReceiver 接收来自game server的消息
    * @param gameMsgSender   向game server发送消息
    **/
  private def working(
    gameMsgReceiver: ActorRef[WsMsgServer],
    gameMsgSender: ActorRef[WsMsgFrontSource],
    loginController: Option[LoginController],
    gameController: Option[GameController],
    botController: Option[BotController],
    roomController: Option[RoomController],
    stageContext: StageContext
  )(
    implicit timer: TimerScheduler[WsCommand]
  ): Behavior[WsCommand] =
    Behaviors.receive[WsCommand] { (ctx, msg) =>
      msg match {
        case msg: StartGame =>
          log.debug(s"get msg: $msg")
          var frameRate = AppSettings.frameRate
          if (botController.isDefined) frameRate = botController.get.frameRate
          if (gameMsgSender != null) {
            gameMsgSender ! GAStartGame(msg.roomId, msg.pwd, frameRate)
          } else {
            timer.startSingleTimer(TimerKey4StartGame, msg, 5.seconds)
          }

          botController match {
            case Some(bc) =>
              ClientBoot.addToPlatform {
                bc.start()
//                  stageContext.switchToLayer(bc.getLs.getScene)
              }


            case None =>
              ClientBoot.addToPlatform {
                gameController.foreach { gc =>
                  gc.start()
                  stageContext.switchScene(gc.getGs.getScene, fullScreen = true, resize = true, isSetOffX = true)
                }
              }
          }

          Behaviors.same

        case msg: CreateRoom =>
          log.debug(s"get msg: $msg")
          var frameRate = AppSettings.frameRate
          if (botController.isDefined) frameRate = botController.get.frameRate
          if (gameMsgSender != null) {
            gameMsgSender ! GACreateRoom(msg.psw, frameRate)
          } else {
            timer.startSingleTimer(TimerKey4CreateGame, msg, 500.millis)
          }

          botController match {
            case Some(bc) =>
              ClientBoot.addToPlatform {
                bc.start()
//                if (org.seekloud.thor.common.BotSettings.render)
//                  stageContext.switchToLayer(bc.getLs.getScene)
              }
            case None =>
              ClientBoot.addToPlatform{
                gameController.foreach{ gc =>
                  gc.start()
                  stageContext.switchScene(gc.getGs.getScene, fullScreen = true, resize = true, isSetOffX = true)
                }}
          }

          Behaviors.same

        case JoinRoomSuccess =>
          if (botController.nonEmpty) {
            botController.get.sdkReplyTo.foreach(_ ! EnterRoomRsp(-1l))
          }

          Behaviors.same

        case msg: JoinRoomFail =>
          botController match {
            case Some(bc) =>
              bc.sdkReplyTo.foreach(_ ! EnterRoomRsp(-1l, 10010, msg.error))
            case None =>
              ClientBoot.addToPlatform {
                roomController.foreach(_.callBackWarning(msg.error))
              }
          }

          Behaviors.same

        case msg: CreateRoomRsp =>

          ClientBoot.addToPlatform {
            if (botController.nonEmpty) { //bot模式
              botController.get.sdkReplyTo.foreach(_ ! EnterRoomRsp(msg.roomId))
            }
            roomController.foreach(_.finalRoomId = msg.roomId)
          }
          Behaviors.same

        case msg: GetLoginController =>
          working(gameMsgReceiver, gameMsgSender, Some(msg.loginController), gameController, botController, roomController, stageContext)

        case msg: GetRoomController =>
          working(gameMsgReceiver, gameMsgSender, loginController, gameController, botController, Some(msg.roomController), stageContext)

        case msg: EstablishConnection2Es =>
          log.info(s"get msg: $msg")
          val webSocketFlow = Http().webSocketClientFlow(WebSocketRequest(msg.wsUrl))
          val source = getSource(ctx.self)
          val sink = getLoginSink(ctx.self)
          val response =
            source
              .viaMat(webSocketFlow)(Keep.right)
              .toMat(sink)(Keep.left)
              .run()
          val connected = response.flatMap { upgrade =>
            if (upgrade.response.status == StatusCodes.SwitchingProtocols) {
              Future.successful(s"EstablishConnection2Es successful.")
            } else {
              throw new RuntimeException(s"WSClient connection failed: ${upgrade.response.status}")
            }
          } //链接建立时
          connected.onComplete(i => log.info(i.toString))
          Behaviors.same

        case msg: GetLoginInfo =>
          val a = System.currentTimeMillis()
//          val layerScene = new LayerScene
//          val bc = new BotController(ctx.self,  msg.playerId, stageContext, layerScene)
          val gameScene = new GameScene
          val gc = new GameController(ctx.self, PlayerInfo(msg.playerId, msg.name), stageContext, gameScene)
          val b = System.currentTimeMillis()
          println(s"create time is ${b - a}")
          log.info(s"get msg: $msg")
          EsheepClient.linkGame(msg.token, msg.playerId).map {
            case Right(rst) =>
              if (rst.errCode == 0) {
                log.info(s"link game accessCode: ${rst.data.accessCode}")
                val url = Routes.clientLinkGame(msg.playerId, msg.name, rst.data.accessCode)
                log.debug(s"link game url: $url")
                val webSocketFlow = Http().webSocketClientFlow(WebSocketRequest(url))
                val source = getSource(ctx.self)
//                val sink = getSink4Server(gameMsgReceiver, Left(bc))
                val sink = getSink4Server(gameMsgReceiver, Right(gc))
                val (stream, response) =
                  source
                    .viaMat(webSocketFlow)(Keep.both)
                    .toMat(sink)(Keep.left)
                    .run()

                val connected = response.flatMap { upgrade =>
                  if (upgrade.response.status == StatusCodes.SwitchingProtocols) {
                    ctx.self ! GetSender(stream)
                    Future.successful(s"link game server success.")
                  } else {
                    throw new RuntimeException(s"link game server failed: ${upgrade.response.status}")
                  }
                } //链接建立时
                connected.onComplete(i => log.info(i.toString))
              } else {
                ClientBoot.addToPlatform {
                  WarningDialog.initWarningDialog(s"${rst.msg}")
                }
              }
            case Left(e) =>
              log.error(s"link game server error: $e")
          }
          loginController.foreach(_.switchToRoomScene(
            ClientUserInfo(msg.playerId, msg.name, msg.token, Some(msg.tokenExistTime)),
            ctx.self
          ))
          gc.checkAndChangePreCanvas()
          println(s"has player Info ${(msg.playerId,msg.name)}")
//          ctx.self ! PlayerInfo(msg.playerId,msg.name)
//          working(gameMsgReceiver, gameMsgSender, loginController, Some(gc), Some(bc), roomController, stageContext)
          working(gameMsgReceiver, gameMsgSender, loginController, Some(gc), botController, roomController, stageContext)

        case msg: BotLogin =>
          val layerScene = new LayerScene
          val bc = new BotController(ctx.self, "bot" + msg.botId, stageContext, layerScene, msg.botFrame)
          EsheepClient.getBotToken(msg.botId, msg.botKey).map {
            case Right(tokenRst) =>
              if (tokenRst.errCode == 0) {
                val botToken = tokenRst.data.token
                val botName = tokenRst.data.botName
                val playerId = s"bot${msg.botId}"
                EsheepClient.linkGame(botToken, playerId).map {
                  case Right(linkRst) =>
                    if (linkRst.errCode == 0) {
                      val accessCode = linkRst.data.accessCode
                      val url = Routes.clientLinkGame(playerId, botName, accessCode)
                      val webSocketFlow = Http().webSocketClientFlow(WebSocketRequest(url))
                      val source = getSource(ctx.self)
                      val sink = getSink4Server(gameMsgReceiver, Left(bc))
                      val (stream, response) =
                        source
                          .viaMat(webSocketFlow)(Keep.both)
                          .toMat(sink)(Keep.left)
                          .run()
                      val connected = response.flatMap { upgrade =>
                        if (upgrade.response.status == StatusCodes.SwitchingProtocols) {
                          ctx.self ! GetSender(stream)

                          //启动bot相关服务
                          val botActor = system.spawn(BotActor.create(ctx.self, bc), "botActor")
                          val port = BotSettings.botServerPort
                          ClientBoot.sdkServerHandler ! SdkServerHandler.BuildBotServer(port, executor, botActor, bc)
                          ClientBoot.addToPlatform{
                            if (org.seekloud.thor.common.BotSettings.render)
                              {
                                stageContext.switchToLayer(layerScene.getScene)
                                layerScene.drawWait()
                              }
                          }
//                          timer.startSingleTimer(TimerKeyForTest, ClientTest(1),5.seconds)
                          Future.successful(s"link game server success.")
                        } else {
                          throw new RuntimeException(s"link game server failed: ${upgrade.response.status}")
                        }
                      } //链接建立时
                      connected.onComplete(i => log.info(i.toString))

                    } else {
                      WarningDialog.initWarningDialog(s"${linkRst.msg}")
                    }
                  case Left(e) =>
                    log.error(s"bot [${msg.botId}] link game error: $e")
                }

              } else {
                WarningDialog.initWarningDialog(s"${tokenRst.msg}")
              }
            case Left(e) =>
              log.error(s"bot [${msg.botId}] get token error: $e")
          }

          working(gameMsgReceiver, gameMsgSender, loginController, gameController, Some(bc), roomController, stageContext)


        case msg: GetSender =>
          log.debug(s"get sender success.")
          working(gameMsgReceiver, msg.stream, loginController, gameController, botController, roomController, stageContext)

        case msg: DispatchMsg =>
          gameMsgSender ! msg.msg
          Behaviors.same

        case ClientTest(roomId) =>
          log.info("get clientTest")

          val rsp1 = client.joinRoom("2", "123") //change pwd
          rsp1.onComplete{
            a=>println(a)
              println("======")
              timer.startSingleTimer(TimerKeyForTest, ActionTest(), 5.seconds)
          }

          Behavior.same

        case GetObservationTest() =>
          log.info("get observationTest")
          val t = System.currentTimeMillis()
          val rsp = client.observation()
          rsp.onComplete{
            a=>println(a)
              println("======")
              timer.startSingleTimer(TimerKeyForTest, ActionTest(), 5.seconds)
          }
          //						timer.startSingleTimer(LeaveRoomKey, LeaveRoomTest(), 5.seconds)
          Behaviors.same

        case ActionSpaceTest() =>
          log.info("get action space Test")

          val rsp1 = client.actionSpace()
          rsp1.onComplete{
            a=>println(a)
              println("======")
              timer.startSingleTimer(TimerKeyForTest, ActionTest(), 5.seconds)
          }
          Behaviors.same

        case LeaveRoomTest() =>
          log.info("get leaveRoomTest")

          val rsp1 = client.leaveRoom()
//          gameMsgSender ! CompleteMsgFrontServer
          rsp1.onComplete{
            a=>println(a)
              println("======")
          }
          Behaviors.same

        case ActionTest() =>

          log.info("get ActionTest")
          val rsp1 = client.action()
          rsp1.onComplete{
            a=>println(a)
              println("======")
            timer.startSingleTimer(TimerKeyForTest, ActionTest(), 10.seconds)
          }
          Behaviors.same

        case SystemInfoTest() =>
          log.info("get frameRate")
          val rsp1 = client.systemInfo()
          rsp1.onComplete{
            a => println(a)
              println("=====")
//              timer.startSingleTimer(TimerKeyForTest, SystemInfoTest(), 5.seconds)
          }
          Behaviors.same

        case Stop =>
          log.info(s"wsClient stopped.")
          Behaviors.stopped

        case x =>
          log.warn(s"unknown msg: $x")
          Behaviors.same
      }
    }


  def getSource(wsClient: ActorRef[WsCommand]): Source[BinaryMessage.Strict, ActorRef[WsMsgFrontSource]] =
    ActorSource.actorRef[WsMsgFrontSource](
      completionMatcher = {
        case CompleteMsgFrontServer =>
          log.info("WebSocket Complete.")
          wsClient ! Stop
      },
      failureMatcher = {
        case FailMsgFrontServer(ex) ⇒ ex
      },
      bufferSize = 8,
      overflowStrategy = OverflowStrategy.fail
    ).collect {
      case message: WsMsgFront =>
        //println(message)
        val sendBuffer = new MiddleBufferInJvm(409600)
        BinaryMessage.Strict(ByteString(
          message.fillMiddleBuffer(sendBuffer).result()
        ))
    }


  def getLoginSink(self: ActorRef[WsClient.WsCommand]): Sink[Message, Future[Done]] =
    Sink.foreach[Message] {
      case TextMessage.Strict(msg) =>
        import io.circe.generic.auto._
        import io.circe.parser.decode
        log.debug(s"msg from webSocket: $msg")
        decode[ESheepProtocol.WsData](msg) match {
          case Right(rst) =>
            rst match {
              case value: Ws4AgentRsp =>
                if (value.errCode == 0) {
                  val playerId = s"user${value.data.userId}"
                  val name = value.data.nickname
                  val token = value.data.token
                  val tokenExistTime = value.data.tokenExpireTime
                  self ! GetLoginInfo(playerId, name, token, tokenExistTime)
                } else {
                  log.error(s"Ws4AgentRsp error: ${value.msg}")
                }
              case HeartBeat => // do nothing
            }

          case Left(e) =>
            log.error(s"decode MsgFromWs error: $e")

        }
      case x =>
        log.debug(s"receive unknown msg: $x")

    }


  def getSink4Server(gameMsgReceiver: ActorRef[ThorGame.WsMsgServer],
    gameController: Either[BotController, GameController]): Sink[Message, Future[Done]] = {
    log.debug(s"getSink4Server...")
    Sink.foreach[Message] {
      case TextMessage.Strict(msg) =>
        gameController match {
          case Right(gc) =>
            gc.wsMessageHandle(ThorGame.TextMsg(msg))
          case Left(bc) =>
            bc.wsMessageHandle(ThorGame.TextMsg(msg))
        }
//        gameController.wsMessageHandle(ThorGame.TextMsg(msg))
        gameMsgReceiver ! ThorGame.TextMsg(msg)

      case BinaryMessage.Strict(bMsg) =>
        val buffer = new MiddleBufferInJvm(bMsg.asByteBuffer)
        val message = bytesDecode[ThorGame.WsMsgServer](buffer) match {
          case Right(rst) => rst
          case Left(e) =>
            log.error(s"decode bMsg error: $e")
            ThorGame.DecodeError()
        }
        gameController match {
          case Right(gc) =>
            gc.wsMessageHandle(message)
          case Left(bc) =>
            bc.wsMessageHandle(message)
        }
        gameMsgReceiver ! message

      case msg: BinaryMessage.Streamed =>
        val futureMsg = msg.dataStream.runFold(new ByteStringBuilder().result()) {
          case (s, str) => s.++(str)
        }
        futureMsg.map { bMsg =>
          val buffer = new MiddleBufferInJvm(bMsg.asByteBuffer)
          val message = bytesDecode[ThorGame.WsMsgServer](buffer) match {
            case Right(rst) => rst
            case Left(e) =>
              println(s"decode streamed bMsg error: $e")
              ThorGame.DecodeError()
          }
          gameController match {
            case Right(gc) =>
              gc.wsMessageHandle(message)
            case Left(bc) =>
              bc.wsMessageHandle(message)
          }
          gameMsgReceiver ! message
        }

      case _ => //do nothing

    }
  }


}
