package actors

import akka.actor.{Actor, ActorRef, Props}
import com.google.inject.name._
import model.{AddCarForm, Car, CarsModel}
import play.api.libs.json._
import play.api.libs.functional.syntax._

@Named("CarsActor")
class CarsActor(out: ActorRef)() extends Actor with CarsModel {
  def receive: Receive = {
    case msg: JsValue => out ! msg
      val possibleSocketRequest = Json.fromJson[InMessage](msg)
      possibleSocketRequest.asOpt foreach {
        request => self ! request
      }
    case socketResponse: SocketResponse =>
      out ! socketResponse.response
    case inMessage: InMessage =>
      inMessage match {
        case InMessage("authenticate", None, None, newCar) => self ! SocketResponse(Json.toJson(insertCar(newCar.head.name, newCar.head.color).getOrElse(cars)))
        case InMessage("cars", None, None, None) => self ! SocketResponse(Json.toJson(getAllCars.getOrElse(List.empty[Car])))
        case InMessage("update", key, car, None) => self ! SocketResponse(Json.toJson(updateCar(car.head, key.head).getOrElse(List.empty[Car])))
        case InMessage("delete", None, car, None) => self ! SocketResponse(Json.toJson(deleteCar(car.head).getOrElse(List.empty[Car])))
        case _ => self ! SocketResponse(JsString("INVALID REQUEST"))
      }
    case _ => self ! SocketResponse(JsString("FAILED TO CONNECT"))
  }
}

object CarsActor {
  def props(out: ActorRef) = Props(new CarsActor(out)())
}

case class SocketResponse(response: JsValue)

case class InMessage(request: String, key: Option[String], car: Option[Car], newCar: Option[AddCarForm])

object InMessage {
  implicit val jsonFormat = Json.format[InMessage]
  implicit val inMessageReads: Reads[InMessage] = (
    (JsPath \ "request").read[String] and
      (JsPath \ "key").readNullable[String] and
      (JsPath \ "car").readNullable[Car] and
      (JsPath \ "newCar").readNullable[AddCarForm]
    ) (InMessage.apply _)
}
