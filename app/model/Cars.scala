package model

import play.api.libs.functional.syntax._
import play.api.libs.json._

object CarsModel {
  var cars = List.empty[Car]

  private def calcX(currentPosition: Double, operator: Char, orientation: Int): Double = operator match {
    case '+' => currentPosition + (Math.cos(Math.toRadians(orientation)) * 10)
    case '-' => currentPosition - (Math.cos(Math.toRadians(orientation)) * 10)
    case _ => currentPosition
  }

  private def calcY(currentPosition: Double, operator: Char, orientation: Int): Double = operator match {
    case '+' => currentPosition + (Math.sin(Math.toRadians(orientation)) * 10)
    case '-' => currentPosition - (Math.sin(Math.toRadians(orientation)) * 10)
    case _ => currentPosition
  }

  private def adjustPosition(car: Car, key: String) = key match {
    case "ArrowUp" => car.copy(position = Map("x" -> calcX(car.position.get("x").head, '+', car.orientation), "y" ->  calcY(car.position.get("y").head, '+', car.orientation)))
    case "ArrowDown" => car.copy(position = Map("x" -> calcX(car.position.get("x").head, '-', car.orientation), "y" ->  calcY(car.position.get("y").head, '-', car.orientation)))
    case "ArrowLeft" => car.copy(orientation = car.orientation - 10)
    case "ArrowRight" => car.copy(orientation = car.orientation + 10)
    case _ => car
  }

  def updateCar(car: Car, key: String): Option[List[Car]] = {
    val updatedCar = adjustPosition(car, key)
    cars = cars.map { c =>
      if (c.name == updatedCar.name) {
        updatedCar
      } else c
    }
    Some(cars)
  }

  def getAllCars: Option[List[Car]] = Some(cars)

  def deleteCar(car: Car): Option[List[Car]] = {
    cars = cars.filterNot(_.name == car.name)
    Some(cars)
  }

  def insertCar(name: String, color: String): Option[List[Car]] = {
    if (cars.exists(_.name == name)) {
      None
    } else {
      cars = Car(Map("x" -> 10, "y" -> 10), 0, 100, name, color) :: cars
      Some(cars)
    }
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

case class InMessage(request: String, key: Option[String], car: Option[Car])
object InMessage {
  implicit val jsonFormat = Json.format[InMessage]
  implicit val inMessageReads: Reads[InMessage] = (
  (JsPath \ "request").read[String] and
  (JsPath \ "key").readNullable[String] and
  (JsPath \ "car").readNullable[Car]
  )(InMessage.apply _)
}

