package actors

import akka.actor.{Actor, ActorRef, Props}
import com.google.inject.name._
import model.{AddCarForm, Car, CarsModel}
import play.api.libs.json._
import play.api.libs.functional.syntax._

@Named("CarsActor")
class CarsActor(out: ActorRef)() extends Actor with CarsModel {
  def receive: Receive = {
    case msg: JsValue =>
      val possibleSocketRequest = Json.fromJson[InMessage](msg)
      possibleSocketRequest.asOpt foreach {
        request => self ! request
      }
    case socketResponse: SocketResponse =>
      out ! Json.toJson(socketResponse.response)
    case inMessage: InMessage =>
      inMessage match {
        case InMessage("authenticate", None, None, newCar) if newCar.isDefined && newCar.head.isInstanceOf[AddCarForm] =>
          val uCars = insertCar(newCar.head.name, newCar.head.color)
          val response = ResponseData(success = uCars.isDefined, inMessage.request, uCars.getOrElse(cars))
          self ! SocketResponse(response)
        case InMessage("cars", None, None, None) =>
          val uCars = getAllCars
          val response = ResponseData(success = uCars.isDefined, inMessage.request, uCars.getOrElse(cars))
          self ! SocketResponse(response)
        case InMessage("update", key, car, None) if key.isDefined && car.isDefined && car.head.isInstanceOf[Car] =>
          val uCars = updateCar(car.head, key.head)
          val response = ResponseData(success = uCars.isDefined, inMessage.request, uCars.getOrElse(cars))
          self ! SocketResponse(response)
        case InMessage("delete", None, car, None) if car.isDefined && car.get.isInstanceOf[Car] =>
          val uCars = deleteCar(car.head)
          val response = ResponseData(success = uCars.isDefined, inMessage.request, uCars.getOrElse(cars))
          self ! SocketResponse(response)
        case _ =>
          self ! SocketResponse(ResponseData(success = false, inMessage.request, cars))
      }
    case _ => self ! SocketResponse(ResponseData(success = false, "INVALID INPUT", cars))
  }
}

object CarsActor {
  def props(out: ActorRef) = Props(new CarsActor(out)())
}

case class SocketResponse(response: ResponseData)

case class ResponseData(success: Boolean, request: String, cars: List[Car])

object ResponseData {
  val responseDataReads: Reads[ResponseData] = (
    (JsPath \ "success").read[Boolean] and
      (JsPath \ "request").read[String] and
      (JsPath \ "cars").read[List[Car]]
    ) (ResponseData.apply _)

  val responseDataWrites: Writes[ResponseData] = (
    (JsPath \ "success").write[Boolean] and
      (JsPath \ "request").write[String] and
      (JsPath \ "cars").write[List[Car]]
    ) (unlift(ResponseData.unapply))

  implicit val responseDataFormat: Format[ResponseData] =
    Format(responseDataReads, responseDataWrites)
}

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
