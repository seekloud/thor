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
import org.seekloud.byteobject.MiddleBufferInJvm
import org.seekloud.thor.ClientBoot
import org.seekloud.thor.common.{Routes, StageContext}
import org.seekloud.thor.controller.{GameController, LoginController}
import org.seekloud.thor.controller.{LoginController, RoomController}
import org.seekloud.thor.shared.ptcl.protocol.ThorGame._
import org.seekloud.thor.ClientBoot.{executor, materializer, system}
import org.seekloud.thor.protocol.{ESheepProtocol, ThorClientProtocol}
import org.seekloud.thor.protocol.ESheepProtocol.{HeartBeat, Ws4AgentRsp}
import org.seekloud.thor.protocol.ThorClientProtocol.ClientUserInfo
import org.seekloud.thor.scene.GameScene
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

  final case class StartGame(roomId: Long) extends WsCommand

  final case object TimerKey4StartGame

  final case class CreateRoom(psw: Option[String]) extends WsCommand

  final case object TimerKey4CreateGame

  final case class JoinRoomFail(error: String) extends WsCommand

  final case class CreateRoomRsp(roomId: Long) extends WsCommand

  final case class DispatchMsg(msg: ThorGame.WsMsgFront) extends WsCommand

  final case class PlayerInfo(playerId: String, name: String) extends WsCommand

  final case class BotLogin(botId: String, botKey: String) extends WsCommand

  final case object Stop extends WsCommand

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
        working(gameMsgReceiver, gameMsgSender = null, None, None, None, stageContext)
      }
    }


  private def working(
    gameMsgReceiver: ActorRef[WsMsgServer],
    gameMsgSender: ActorRef[WsMsgFrontSource],
    loginController: Option[LoginController],
    gameController: Option[GameController],
    roomController: Option[RoomController],
    stageContext: StageContext
  )(
    implicit timer: TimerScheduler[WsCommand]
  ): Behavior[WsCommand] =
    Behaviors.receive[WsCommand] { (ctx, msg) =>
      msg match {
        //        case msg: PlayerIdName =>
        //          println(s"get player info $msg")
        //         working(gameMsgReceiver, gameMsgSender, loginController, Some(msg) , roomController, stageContext)
        case msg: StartGame =>
          log.debug(s"get msg: $msg")
          if (gameMsgSender != null) {
            gameMsgSender ! GAStartGame(msg.roomId)
          } else {
            timer.startSingleTimer(TimerKey4StartGame, msg, 2.seconds)
          }
          ClientBoot.addToPlatform {
            gameController.foreach { gc =>
              gc.start()
              stageContext.switchScene(gc.getGs.getScene, fullScreen = true, resize = true)
            }
          }
          //TODO GameController
          //          ClientBoot.addToPlatform {
          //            gameController.foreach{ p =>
          //              roomController.foreach { r =>
          //                println("creating new scene")
          //                val gameScene = new GameScene
          //                stageContext.switchScene(gameScene.getScene, fullScreen = true)
          //                println("creating new controller")
          //                new GameController(ctx.self, PlayerInfo(p.playerId, p.name), stageContext, gameScene ).start()
          //              }
          //            }
          //          }
          Behaviors.same

        case msg: CreateRoom =>
          log.debug(s"get msg: $msg")
          if (gameMsgSender != null) {
            gameMsgSender ! GACreateRoom(msg.psw)
          } else {
            timer.startSingleTimer(TimerKey4CreateGame, msg, 2.seconds)
          }
          ClientBoot.addToPlatform {
            gameController.foreach { gc =>
              gc.start()
              stageContext.switchScene(gc.getGs.getScene, fullScreen = true, resize = true)
            }
          }
          //TODO GameController
          Behaviors.same

        case msg: DispatchMsg =>
          gameMsgSender ! msg.msg
          Behaviors.same

        case msg: JoinRoomFail =>
          ClientBoot.addToPlatform {
            roomController.foreach(_.callBackWarning(msg.error))
          }
          Behaviors.same

        case msg: CreateRoomRsp =>
          ClientBoot.addToPlatform {
            roomController.foreach(_.finalRoomId == msg.roomId)
          }
          Behaviors.same

        case msg: GetLoginController =>
          working(gameMsgReceiver, gameMsgSender, Some(msg.loginController), gameController, roomController, stageContext)

        case msg: GetRoomController =>
          working(gameMsgReceiver, gameMsgSender, loginController, gameController, Some(msg.roomController), stageContext)

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
          val gameScene = new GameScene
          val gc = new GameController(ctx.self, PlayerInfo(msg.playerId, msg.name), stageContext, gameScene)
          val b = System.currentTimeMillis()
          println(s"create time is ${b - a}")
          log.info(s"get msg: $msg")
          EsheepClient.linkGame(msg.token, msg.playerId).map {
            case Right(rst) =>
              if (rst.errCode == 0) {
                log.info(s"link game accessCode: ${rst.data.accessCode}")
                //TODO 与game server建立ws连接 连接已建立，sender向server发送，receiver接收server消息
                val url = Routes.clientLinkGame(msg.playerId, msg.name, rst.data.accessCode)
                log.debug(s"link game url: $url")
                val webSocketFlow = Http().webSocketClientFlow(WebSocketRequest(url))
                val source = getSource(ctx.self)
                val sink = getSink4Server(gameMsgReceiver, gc)
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
          println(s"has player Info ${(msg.playerId, msg.name)}")
          //          ctx.self ! PlayerInfo(msg.playerId,msg.name)
          working(gameMsgReceiver, gameMsgSender, loginController, Some(gc), roomController, stageContext)

        case msg: BotLogin =>
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
                      //TODO 创建bot的server sink
//                      val sink = getSink4Server(gameMsgReceiver, )
//                      val (stream, response) =
//                        source
//                          .viaMat(webSocketFlow)(Keep.both)
//                          .toMat(sink)(Keep.left)
//                          .run()

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

          Behaviors.same


        case msg: GetSender =>
          log.debug(s"get sender success.")
          working(gameMsgReceiver, msg.stream, loginController, gameController, roomController, stageContext)

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

  def getSink4Server(gameMsgReceiver: ActorRef[ThorGame.WsMsgServer], gameController: GameController): Sink[Message, Future[Done]] = {
    log.debug(s"getSink4Server...")
    Sink.foreach[Message] {
      case TextMessage.Strict(msg) =>
        gameController.wsMessageHandle(ThorGame.TextMsg(msg))
        gameMsgReceiver ! ThorGame.TextMsg(msg)

      case BinaryMessage.Strict(bMsg) =>
        val buffer = new MiddleBufferInJvm(bMsg.asByteBuffer)
        val message = bytesDecode[ThorGame.WsMsgServer](buffer) match {
          case Right(rst) => rst
          case Left(e) =>
            log.error(s"decode bMsg error: $e")
            ThorGame.DecodeError()
        }
        gameController.wsMessageHandle(message)
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
          gameController.wsMessageHandle(message)
          gameMsgReceiver ! message
        }

      case _ => //do nothing

    }
  }


}
