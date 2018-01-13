package controllers

import javax.inject._

import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc._
import play.api.libs.streams._
import akka.actor._
import akka.stream.Materializer
import model.{Cars, InMessage}

@Singleton
class HomeController @Inject()(implicit system: ActorSystem, materializer: Materializer, cc: ControllerComponents) extends AbstractController(cc) {

  def appSummary = Action {
    Ok(Json.obj("content" -> "Demolition Derby"))
  }

  def carSocket = WebSocket.accept[JsValue, JsValue] { request =>
    ActorFlow.actorRef(out => CarSocketActor.props(out))
  }
}

object CarSocketActor {
  def props(out: ActorRef) = Props(new CarActor(out))
}

class CarActor(out: ActorRef) extends Actor {

  import model.CarsModel._

  def receive = {
    case msg: JsValue =>
      val inMessage = msg.validate[InMessage].get
      inMessage match {
        case InMessage("cars", None) => out ! Json.toJson(cars)
        case InMessage("update", car) => {
          cars = Cars(cars.cars.map(c => {
            if (c.name == car.head.name) {
              car.head
            } else c
          }))
          out ! Json.toJson(cars)
        }
        case _ => out ! "FAILED"
      }
  }
}


