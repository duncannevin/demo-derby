package model

import play.api.libs.functional.syntax._
import play.api.libs.json._

import scala.concurrent.Future

object CarsModel {
  var cars = List.empty[Car]

  def insertCar(name: String, color: String): Future[Option[List[Car]]] = {
    Future.successful(
      if (cars.exists(_.name == name)) {
        None
      } else {
        cars = Car(Map("x" -> 10, "y" -> 10), 0, 100, name, color) :: cars
        Some(cars)
      }
    )
  }

  def removeCars(): List[Car] = {
    cars = List.empty[Car]
    cars
  }
}

case class Car(position: Map[String, Double], orientation: Int, life: Int, name: String, color: String)
object Car {
  val carReads: Reads[Car] = (
  (JsPath \ "position").read[Map[String, Double]] and
  (JsPath \ "orientation").read[Int] and
  (JsPath \ "life").read[Int] and
  (JsPath \ "name").read[String] and
  (JsPath \ "color").read[String]
  )(Car.apply _)

  val carWrites: Writes[Car] = (
  (JsPath \ "position").write[Map[String, Double]] and
  (JsPath \ "orientation").write[Int] and
  (JsPath \ "life").write[Int] and
  (JsPath \ "name").write[String] and
  (JsPath \ "color").write[String]
  )(unlift(Car.unapply))

  implicit val carFormat: Format[Car] =
    Format(carReads, carWrites)
}

case class AddCarForm(name: String, color: String)
object  AddCarForm {
  implicit val jsonFormat = Json.format[AddCarForm]
  implicit val addCarReads: Reads[AddCarForm] = (
  (JsPath \ "name").read[String] and
  (JsPath \ "color").read[String]
  )(AddCarForm.apply _)
}

case class InMessage(request: String, car: Option[Car])
object InMessage {
  implicit val jsonFormat = Json.format[InMessage]
  implicit val inMessageReads: Reads[InMessage] = (
  (JsPath \ "request").read[String] and
  (JsPath \ "car").readNullable[Car]
  )(InMessage.apply _)
}

