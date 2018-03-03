package model

import java.awt
import java.awt.geom._
import java.awt.{Canvas, Point, Polygon, Rectangle, Shape}

import apple.laf.JRSUIConstants.Orientation
import com.sun.javafx.geom.transform.BaseTransform.Degree
import org.w3c.dom.css.Rect
import play.api.libs.functional.syntax._
import play.api.libs.json._

trait CarsModel {
  var cars = List(Car(Map("x" -> 100.0, "y" -> 250.0), 0, 100, "BOB141", "#fff", getCarCorners((100.0, 250.0), 0)), Car(Map("x" -> 401.0, "y" -> 250.0), 162, 100, "SAL162", "#f0f0f0", getCarCorners((401.0, 250.0), 162)))

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

  private def rotateLeft(car: Car, degrees: Int): Car = {
    val orient = car.orientation - degrees
    if (orient < 0) car.copy(orientation = orient + 360, testCorners = getCarCorners((car.position("x"), car.position("y")), orient))
    else car.copy(orientation = car.orientation - degrees, testCorners = getCarCorners((car.position("x"), car.position("y")), orient))
  }

  private def rotateRight(car: Car, degrees: Int): Car = {
    val orient = car.orientation + degrees % 360
    car.copy(orientation = orient, testCorners = getCarCorners((car.position("x"), car.position("y")), orient))
  }

  private def rotateToo(car: Car, degree: Int): Car = car.copy(orientation = degree)

  private def adjustPosition(car: Car, key: String) = key match {
    case "ArrowUp" =>
      val newPosition = Map("x" -> truncateAt(calcX(car.position.get("x").head, '+', car.orientation), 4), "y" -> truncateAt(calcY(car.position.get("y").head, '+', car.orientation), 4))
      car.copy(position = newPosition, testCorners = getCarCorners((newPosition("x"), newPosition("y")), car.orientation))
    case "ArrowDown" =>
      val newPosition = Map("x" -> truncateAt(calcX(car.position.get("x").head, '-', car.orientation), 4), "y" -> truncateAt(calcY(car.position.get("y").head, '-', car.orientation), 4))
      car.copy(position = newPosition, testCorners = getCarCorners((newPosition("x"), newPosition("y")), car.orientation))
    case "ArrowLeft" => rotateLeft(car, 10)
    case "ArrowRight" => rotateRight(car, 10)
    case _ => car
  }

  /** Middle point on car's top side.*/
  def getRulerTopMiddle(ruler: (Double, Double), cos: Double, sin: Double): (Double, Double) = {
    (
      ruler._1 - sin * 15,
      ruler._2 + cos * 15
    )
  }

  /** Middle point on car's bottom side. */
  def getCarBottomMiddle(ruler: (Double, Double), cos: Double, sin: Double): (Double, Double) = {
    (
      ruler._1 + sin * 15,
      ruler._2 - cos * 15
    )
  }

  /** Update car's four corner coordinates. */
  def getCarCorners(ruler: (Double, Double), orientation: Int): List[(Double, Double)] = {
    val sin = Math.sin(Math.toRadians(orientation))
    val cos = Math.cos(Math.toRadians(orientation))
    val topMiddle = getRulerTopMiddle(ruler, cos, sin)
    val bottomMiddle = getCarBottomMiddle(ruler, cos, sin)

    val nw = (
      topMiddle._1 - (cos * 30),
      topMiddle._2 - (sin * 30)
    )
    val ne = (
      topMiddle._1 + (cos * 30),
      topMiddle._2 + (sin * 30)
    )
    val sw = (
      bottomMiddle._1 - (cos * 30),
      bottomMiddle._2 - (sin * 30)
    )
    val se = (
      bottomMiddle._1 + (cos * 30),
      bottomMiddle._2 + (sin * 30)
    )
    List(nw, ne, sw, se)
  }

  def doPolygonsIntersect(a: List[(Double, Double)], b: List[(Double, Double)]): Boolean = {
    val aPolygon = new Polygon()
    aPolygon.addPoint(a.head._1.toInt, a.head._2.toInt)
    aPolygon.addPoint(a(1)._1.toInt, a(1)._2.toInt)
    aPolygon.addPoint(a(2)._1.toInt, a(2)._2.toInt)
    aPolygon.addPoint(a.last._1.toInt, a.last._2.toInt)

    val bPolygon = new Polygon()
    bPolygon.addPoint(b.head._1.toInt, b.head._2.toInt)
    bPolygon.addPoint(b(1)._1.toInt, b(1)._2.toInt)
    bPolygon.addPoint(b(2)._1.toInt, b(2)._2.toInt)
    bPolygon.addPoint(b.last._1.toInt, b.last._2.toInt)


    val aArea = new Area(aPolygon)
    aArea.intersect(new Area(bPolygon))
    !aArea.isEmpty
  }

  private def collideCheck(offenseCar: Car, defenseCar: Car) = {
    /*
    * ***Collision detection using polygon***
    * */
    doPolygonsIntersect(offenseCar.testCorners, defenseCar.testCorners)

    /*
    * ***Collision detection using radius***
    * x0,y0,r0 = Center and radius of circle 0.
    * x1,y1,r1 = Center and radius of circle 1.
    * */
//    val x0 = offenseCar.position("x")
//    val y0 = offenseCar.position("y")
//    val r0 = 30.0
//    val x1 = defenseCar.position("x")
//    val y1 = defenseCar.position("y")
//    val r1 = 30.0
//    Math.hypot(x0-x1, y0-y1) <= (r0 + r1)
  }

  private def edgeCheck(car: Car): Car = car.position match {
    case p if p("x") < 30.0 => car.copy(position = Map("x" -> 30.0, "y" -> p("y")), life = car.life - 1)
    case p if p("y") < 30.0 => car.copy(position = Map("x" -> p("x"), "y" -> 30.0), life = car.life - 1)
    case p if p("x") > 870.0 => car.copy(position = Map("x" -> 870.0, "y" -> p("y")), life = car.life - 1)
    case p if p("y") > 470.0 => car.copy(position = Map("x" -> p("x"), "y" -> 470.0), life = car.life - 1)
    case _ => car
  }

  def updateCar(car: Car, key: String): Option[List[Car]] = {
    var updatedCar = edgeCheck(adjustPosition(car, key))
    val updatedCars = for {
      c <- cars.filterNot(_.name == updatedCar.name)
      collide =
        if (c.name == updatedCar.name) {
          updatedCar
        } else if (collideCheck(updatedCar, c)) {
          updatedCar = car.copy(life = car.life - 1)
          c.copy(life = c.life - 10)
        } else c
    } yield collide

    cars = (updatedCar :: updatedCars).filter(_.life > 0)

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
      cars = Car(Map("x" -> 200.0, "y" -> 300.0), 0, 100, name, color, getCarCorners((200.0, 300.0), 0)) :: cars
      Some(cars)
    }
  }

  def removeCars(): List[Car] = {
    cars = List.empty[Car]
    cars
  }
}

case class Car(position: Map[String, Double], orientation: Int, life: Int, name: String, color: String, testCorners: List[(Double, Double)])

object Car {
  val carReads: Reads[Car] = (
    (JsPath \ "position").read[Map[String, Double]] and
      (JsPath \ "orientation").read[Int] and
      (JsPath \ "life").read[Int] and
      (JsPath \ "name").read[String] and
      (JsPath \ "color").read[String] and
      (JsPath \ "testCorners").read[List[(Double, Double)]]
    ) (Car.apply _)

  val carWrites: Writes[Car] = (
    (JsPath \ "position").write[Map[String, Double]] and
      (JsPath \ "orientation").write[Int] and
      (JsPath \ "life").write[Int] and
      (JsPath \ "name").write[String] and
      (JsPath \ "color").write[String] and
      (JsPath \ "testCorners").write[List[(Double, Double)]]
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

