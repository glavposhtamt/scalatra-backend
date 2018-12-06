package com.royal.app.routes

import com.royal.app.{ImageStorage, JSONProcessing}

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession
import com.royal.app.Tables._
import org.json4s.jackson.JsonMethods.{compact, parse}
import org.json4s.JsonDSL._
import org.scalatra.ScalatraServlet

trait CategoriesRoutes extends ScalatraServlet with JSONProcessing with ImageStorage {
  val db: Database

  get("/categories") {

    contentType = "application/json"
    response.setHeader("Access-Control-Allow-Origin", "*")

    getAllCategories
  }

  put("/categories/:id") {

    contentType = "application/json"
    response.setHeader("Access-Control-Allow-Origin", "*")

    val id = params("id") toInt

    val (title, selected, base64) = getCategoriesInfo(request.body)
    val image = cloudinary(base64)

    db withDynSession {
      categories insertOrUpdate Widget(id, title, selected, image)
    }

    compact(
        ("id" -> id) ~
        ("title" -> title) ~
        ("selected" -> selected) ~
        ("image" -> image)
    )
  }

  post("/categories") {

    contentType = "application/json"
    response.setHeader("Access-Control-Allow-Origin", "*")

    val (title, selected, base64) = getCategoriesInfo(request.body)
    val image = cloudinary(base64)

    db withDynSession {
      val id = (categories returning categories.map(_.id)) += Widget(0, title, selected, image)

      compact(
          ("id" -> id) ~
          ("title" -> title) ~
          ("selected" -> selected) ~
          ("image" -> image)
      )
    }
  }

  delete("/categories/:id") {

    contentType = "application/json"
    response.setHeader("Access-Control-Allow-Origin", "*")

    val id = params("id") toInt

    db withDynSession {
      categories filter (_.id === id) delete
    }

    getAllCategories
  }

  private def getAllCategories = {
    db withDynSession {
      val objList = categories.list map {
        case category =>
            ("id" -> category.id) ~
            ("title" -> category.title) ~
            ("selected" -> category.selected) ~
            ("image" -> category.image)
      }

      compact(objList)
    }
  }

  private def getCategoriesInfo(request: String): (String, Boolean, String) = {
    val json = parse(request)

    (
      getJsonValue[String]("title", json),
      getJsonValue[Boolean]("selected", json),
      getJsonValue[String]("image", json)
    )
  }
}
