package model

import play.api.libs.functional.syntax._
import play.api.libs.json._

object CarsModel {
  var cars = List(
    Car(Map("x" -> 400.0, "y" -> 300.0), 20, 100, "tony0", "#fff"),
    Car(Map("x" -> 200.0, "y" -> 100.0), 91, 100, "jeb90", "#fff123"),
    Car(Map("x" -> 500.0, "y" -> 200.0), 50, 100, "jen180", "#008080"),
    Car(Map("x" -> 800.0, "y" -> 400.0), 27, 100, "jill270", "#f2f120")
  )

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

  private def truncateAt(n: Double, p: Int): Double = { val s = math pow (10, p); (math floor n * s) / s }

  private def adjustPosition(car: Car, key: String) = key match {
    case "ArrowUp" => car.copy(position = Map("x" -> truncateAt(calcX(car.position.get("x").head, '+', car.orientation), 4), "y" -> truncateAt(calcY(car.position.get("y").head, '+', car.orientation), 4)))
    case "ArrowDown" => car.copy(position = Map("x" -> truncateAt(calcX(car.position.get("x").head, '-', car.orientation), 4), "y" -> truncateAt(calcY(car.position.get("y").head, '-', car.orientation), 4)))
    case "ArrowLeft" =>
      val orient = car.orientation - 10
      if (orient < 0) car.copy(orientation = orient + 360)
      else car.copy(orientation = car.orientation - 10)
    case "ArrowRight" => car.copy(orientation = (car.orientation + 10) % 360)
    case _ => car
  }

  private def collideCheck(offenseCar: Car, defenseCar: Car) = {
    val dx = Math.abs(defenseCar.position("x") - offenseCar.position("x"))
    val dy = Math.abs(defenseCar.position("y") - offenseCar.position("y"))
    val R = 30

    if (dx > R) false
    else if (dy > R) false
    else if (dx + dy <= R) true
    else if (dx * dx + dy * dy <= R * R) true
    else true
  }

  private def edgeCheck(car: Car): Car = car.position match {
    case p if p("x") < 30.0 => car.copy(position = Map("x" -> 30.0, "y" -> p("y")))
    case p if p("y") < 30.0 => car.copy(position = Map("x" -> p("x"), "y" -> 30.0))
    case p if p("x") > 870.0 => car.copy(position = Map("x" -> 870.0, "y" -> p("y")))
    case p if p("y") > 470.0 => car.copy(position = Map("x" -> p("x"), "y" -> 470.0))
    case _ => car
  }

  def updateCar(car: Car, key: String): Option[List[Car]] = {
    var updatedCar = edgeCheck(adjustPosition(car, key))
    cars = cars.filter(_.name != updatedCar.name)
    cars = cars.map { c =>
      if (collideCheck(updatedCar, c)) {
        updatedCar = car
        c.copy(life = c.life - 10)
      } else c
    }
    Some(updatedCar :: cars)
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
      cars = Car(Map("x" -> 200.0, "y" -> 300.0), 0, 100, name, color) :: cars
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
    ) (Car.apply _)

  val carWrites: Writes[Car] = (
    (JsPath \ "position").write[Map[String, Double]] and
      (JsPath \ "orientation").write[Int] and
      (JsPath \ "life").write[Int] and
      (JsPath \ "name").write[String] and
      (JsPath \ "color").write[String]
    ) (unlift(Car.unapply))

  implicit val carFormat: Format[Car] =
    Format(carReads, carWrites)
}

case class AddCarForm(name: String, color: String)

object AddCarForm {
  implicit val jsonFormat = Json.format[AddCarForm]
  implicit val addCarReads: Reads[AddCarForm] = (
    (JsPath \ "name").read[String] and
      (JsPath \ "color").read[String]
    ) (AddCarForm.apply _)
}

case class InMessage(request: String, key: Option[String], car: Option[Car])

object InMessage {
  implicit val jsonFormat = Json.format[InMessage]
  implicit val inMessageReads: Reads[InMessage] = (
    (JsPath \ "request").read[String] and
      (JsPath \ "key").readNullable[String] and
      (JsPath \ "car").readNullable[Car]
    ) (InMessage.apply _)
}

