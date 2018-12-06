package com.royal.app.routes

import com.royal.app.JSONProcessing

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession
import com.royal.app.Tables._
import org.json4s.jackson.JsonMethods.{compact, parse}
import org.json4s.JsonDSL._
import org.scalatra.ScalatraServlet

trait MaterialRoutes extends ScalatraServlet with JSONProcessing {
  val db: Database

  get("/material") {
    contentType = "application/json"
    response.setHeader("Access-Control-Allow-Origin", "*")

    getAllMaterials
  }

  put("/material/:id") {

    contentType = "application/json"
    response.setHeader("Access-Control-Allow-Origin", "*")

    val id = params("id") toInt
    val json = parse(request.body)

    val title = getJsonValue[String]("title", json)

    db withDynSession {
      materials insertOrUpdate(id, title)
    }

    compact(
        ("id" -> id) ~
        ("title" -> title)
    )
  }

  post("/material") {

    contentType = "application/json"
    response.setHeader("Access-Control-Allow-Origin", "*")

    val json = parse(request.body)
    val title = getJsonValue[String]("title", json)

    db withDynSession {
      val id = (materials returning materials.map(_.id)) += (0, title)

      compact(
          ("id" -> id) ~
          ("title" -> title)
      )
    }
  }

  delete("/material/:id") {

    contentType = "application/json"
    response.setHeader("Access-Control-Allow-Origin", "*")

    val id = params("id") toInt

    db withDynSession {
      materials filter(_.id === id) delete
    }

    getAllMaterials
  }

  private def getAllMaterials = {
    db withDynSession {

      val objList = materials.list map {
        case (id, title) =>
            ("id" -> id) ~
            ("title" -> title)
      }

      compact(objList)
    }
  }

}
