package controllers

import javax.inject._

import play.api.libs.json._
import play.api.mvc._
import akka.actor._
import akka.stream.Materializer
import akka.stream.scaladsl._
import actors.CarsActor
import model.AddCarForm
import play.api.libs.streams.ActorFlow

import scala.concurrent.ExecutionContext
import utils.SameOriginCheck

@Singleton
class CarController @Inject()(
                               implicit ec: ExecutionContext,
                               implicit val system: ActorSystem,
                               materializer: Materializer,
                               cc: ControllerComponents,
                             ) extends AbstractController(cc) with SameOriginCheck with model.CarsModel {

  private type WSMessage = JsValue

  /*
  * Send cars to all clients
  * */
  private val (chatSink, chatSource) = {

    val source = MergeHub.source[WSMessage]
    .log("source")
    .recoverWithRetries(-1, { case _: Exception ⇒ Source.empty })

    val sink = BroadcastHub.sink[WSMessage]
    source.toMat(sink)(Keep.both).run()
  }

  /*
  * Handles car websocket
  * */
  def carSocket = WebSocket.accept[JsValue, JsValue] { request =>
    ActorFlow.actorRef { out =>
      CarsActor.props(out)
    }.via(Flow.fromSinkAndSource(chatSink, chatSource)).log("userFlow")
  }
}





















