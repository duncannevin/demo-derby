package model

import play.api.libs.functional.syntax._
import play.api.libs.json._

object CarsModel {
  var cars = Cars(List(
    Car(Map("x" -> 10.0, "y" -> 20.0), 0, 100, "Fred", "teal"),
    Car(Map("x" -> 100.0, "y" -> 200.0), 0, 100, "Ted", "pink")
  ))
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

case class Cars(cars: List[Car])
object Cars {
  implicit val jsonFormat = Json.format[Cars]
  implicit val carsReads: Reads[Cars] = (JsPath \ "cars").read[List[Car]].map(Cars.apply)
}

case class InMessage(request: String, car: Option[Car])
object InMessage {
  implicit val jsonFormat = Json.format[InMessage]
  implicit val inMessageReads: Reads[InMessage] = (
  (JsPath \ "request").read[String] and
  (JsPath \ "car").readNullable[Car]
  )(InMessage.apply _)
}

