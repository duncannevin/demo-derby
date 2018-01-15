package controllers

import javax.inject._
import play.api.libs.json._
import play.api.mvc._
import akka.actor._
import akka.stream.Materializer
import akka.stream.scaladsl._
import model.CarsModel._
import model.{AddCarForm, InMessage}
import scala.concurrent.{ExecutionContext, Future}
import utils.SameOriginCheck
import play.api.libs.functional.syntax._

@Singleton
class CarController @Inject()(implicit ec: ExecutionContext, implicit val system: ActorSystem, materializer: Materializer, cc: ControllerComponents) extends AbstractController(cc) with SameOriginCheck {

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
  * Gather cars, or update car -> always return all cars to update state.
  * */
  private val userFlow: Flow[WSMessage, WSMessage, _] = {
    Flow[WSMessage].map { msg =>
      val inMessage = msg.validate[InMessage].get
      inMessage match {
        case InMessage("cars", None) => Json.toJson(cars)
        case InMessage("update", car) => {
          cars = cars.map(c => {
            if (c.name == car.head.name) {
              car.head
            } else c
          })
          Json.toJson(cars)
        }
        case InMessage("delete", car) => {
          cars = cars.filterNot(_.name == car.head.name)
          Json.toJson(cars)
        }
        case _ => JsString("FAILED")
      }
    }.via(Flow.fromSinkAndSource(chatSink, chatSource)).log("userFlow")
  }

  /*
  * Handles car websocket
  * */
  def carSocket: WebSocket = {
    WebSocket.acceptOrResult[WSMessage, WSMessage] {
      case rh if sameOriginCheck(rh) =>
        Future.successful(userFlow).map { flow =>
          Right(flow)
        }.recover {
          case e: Exception =>
            val msg = "Cannot create websocket"
            println(msg, e)
            val result = InternalServerError(msg)
            Left(result)
        }

      case rejected =>
        println(s"Request ${rejected} failed same origin check")
        Future.successful {
          Left(Forbidden("forbidden"))
        }
    }
  }

  def addCar = Action.async(parse.json) { implicit request: Request[JsValue] =>
    val vBody = request.body.validate[AddCarForm]
    if (vBody.isSuccess) {
      val form = vBody.get
      for {
        add <- insertCar(name = form.name, color = form.color)
      } yield add match {
        case None => Ok(Json.toJson("Name already exists"))
        case Some(c) => Ok(Json.toJson(c))
      }
    } else Future.successful(BadRequest)
  }

  def deleteCars = Action {
    Ok(Json.toJson(removeCars()))
  }
}





















