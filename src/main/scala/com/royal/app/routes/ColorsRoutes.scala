package com.royal.app.routes

import com.royal.app.JSONProcessing

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession
import com.royal.app.Tables.{Color, colors}
import org.json4s.jackson.JsonMethods.{compact, parse}
import org.json4s.JsonDSL._
import org.scalatra.ScalatraServlet

trait ColorsRoutes extends ScalatraServlet with JSONProcessing {
  val db: Database

  get("/colors") {

    contentType = "application/json"
    response.setHeader("Access-Control-Allow-Origin", "*")

    getAllColors
  }

  put("/colors/:id") {

    contentType = "application/json"
    response.setHeader("Access-Control-Allow-Origin", "*")

    val id = params("id") toInt
    val json = parse(request.body)

    val title = getJsonValue[String]("title", json)
    val hex = getJsonValue[String]("hex", json)

    db withDynSession {
      colors insertOrUpdate Color(id, title, hex)
    }

    compact(
        ("id" -> id) ~
        ("title" -> title) ~
        ("hex" -> hex)
    )
  }

  post("/colors") {

    contentType = "application/json"
    response.setHeader("Access-Control-Allow-Origin", "*")

    val json = parse(request.body)

    val title = getJsonValue[String]("title", json)
    val hex = getJsonValue[String]("hex", json)

    db withDynSession {
      val id = (colors returning colors.map(_.id)) += Color(0, title, hex)
      compact(
          ("id" -> id) ~
          ("title" -> title) ~
          ("hex" -> hex)
      )
    }
  }

  delete("/colors/:id") {

    contentType = "application/json"
    response.setHeader("Access-Control-Allow-Origin", "*")

    val id = params("id") toInt

    db withDynSession {
      colors filter (_.id === id) delete
    }

    getAllColors
  }

  private def getAllColors = {
    db withDynSession {

      val objList = colors.list map {
        case Color(id, title, hex) =>
            ("id" -> id) ~
            ("title" -> title) ~
            ("hex" -> hex)
      }

      compact(objList)
    }
  }
}
