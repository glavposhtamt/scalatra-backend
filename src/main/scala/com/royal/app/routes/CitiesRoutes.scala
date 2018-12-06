package com.royal.app.routes

import com.royal.app.JSONProcessing

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession
import com.royal.app.Tables._
import org.json4s.jackson.JsonMethods.{compact, parse}
import org.json4s.JsonDSL._
import org.scalatra.ScalatraServlet

trait CitiesRoutes extends ScalatraServlet with JSONProcessing {
  val db: Database

  get("/cities") {
    contentType = "application/json"
    response.setHeader("Access-Control-Allow-Origin", "*")

    getAllCities
  }

  put("/cities/:id") {

    contentType = "application/json"
    response.setHeader("Access-Control-Allow-Origin", "*")

    val id = params("id") toInt
    val (name, latitude, longitude, phone) = getCitiesInfo(request.body)

    db withDynSession {
      cities insertOrUpdate(id, name, latitude, longitude, phone)
    }

    compact(
        ("id" -> id) ~
        ("name" -> name) ~
        ("latitude" -> latitude) ~
        ("longitude" -> longitude) ~
        ("phone" -> phone)
    )
  }

  post("/cities") {

    contentType = "application/json"
    response.setHeader("Access-Control-Allow-Origin", "*")

    val (name, latitude, longitude, phone) = getCitiesInfo(request.body)

    db withDynSession {
      val id = (cities returning cities.map(_.id)) += (0, name, latitude, longitude, phone)

      compact(
          ("id" -> id) ~
          ("name" -> name) ~
          ("latitude" -> latitude) ~
          ("longitude" -> longitude) ~
          ("phone" -> phone)
      )
    }
  }

  delete("/cities/:id") {

    contentType = "application/json"
    response.setHeader("Access-Control-Allow-Origin", "*")

    val id = params("id") toInt

    db withDynSession {
      cities filter(_.id === id) delete
    }

    getAllCities
  }

  private def getAllCities = {
    db withDynSession {

      val objList = cities.list map {
        case (id, name, latitude, longitude, phone) =>
              ("id" -> id) ~
              ("name" -> name) ~
              ("latitude" -> latitude) ~
              ("longitude" -> longitude) ~
              ("phone" -> phone)
      }

      compact(objList)
    }
  }

 private def getCitiesInfo(request: String): (String, Double, Double, String) = {
   val json = parse(request)

   (
     getJsonValue[String]("name", json),
     getJsonValue[Double]("latitude", json),
     getJsonValue[Double]("longitude", json),
     getJsonValue[String]("phone", json)
   )
 }
}
