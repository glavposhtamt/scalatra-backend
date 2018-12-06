package com.royal.app.routes

import com.royal.app.{ImageStorage, JSONProcessing}

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession
import com.royal.app.Tables._
import org.json4s.jackson.JsonMethods.{compact, parse}
import org.json4s.JsonDSL._
import org.scalatra.ScalatraServlet

trait BrandsRoutes extends ScalatraServlet  with JSONProcessing with ImageStorage{
  val db: Database

  get("/brands") {

    contentType = "application/json"
    response.setHeader("Access-Control-Allow-Origin", "*")

    getAllBrands
  }

  put("/brands/:id") {

    contentType = "application/json"
    response.setHeader("Access-Control-Allow-Origin", "*")

    val id = params("id") toInt

    val (title, selected, base64) = getBrandsInfo(request.body)
    val image = cloudinary(base64)

    db withDynSession {
      brands insertOrUpdate Widget(id, title, selected, image)
    }

    compact(
        ("id" -> id) ~
        ("title" -> title) ~
        ("selected" -> selected) ~
        ("image" -> image)
    )
  }

  post("/brands") {

    contentType = "application/json"
    response.setHeader("Access-Control-Allow-Origin", "*")

    val (title, selected, base64) = getBrandsInfo(request.body)
    val image = cloudinary(base64)

    db withDynSession {
      val id = (brands returning brands.map(_.id)) += Widget(0, title, selected, image)

      compact(
          ("id" -> id) ~
          ("title" -> title) ~
          ("selected" -> selected) ~
          ("image" -> image)
      )
    }
  }

  delete("/brands/:id") {

    contentType = "application/json"
    response.setHeader("Access-Control-Allow-Origin", "*")

    val id = params("id") toInt

    db withDynSession {
      brands filter (_.id === id) delete
    }

    getAllBrands
  }

  private def getAllBrands = {
    db withDynSession {

      db withDynSession {
        val objList = brands.list map {
          case brand =>
              ("id" -> brand.id) ~
              ("title" -> brand.title) ~
              ("selected" -> brand.selected) ~
              ("image" -> brand.image)
        }

        compact(objList)
      }
    }
  }

  private def getBrandsInfo(request: String): (String, Boolean, String) = {
    val json = parse(request)

    (
      getJsonValue[String]("title", json),
      getJsonValue[Boolean]("selected", json),
      getJsonValue[String]("image", json)
    )
  }
}
