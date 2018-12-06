package com.royal.app.routes

import com.royal.app.{JSONProcessing, NotFoundJSONFieldException}

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession
import com.royal.app.Tables._
import org.json4s.JsonAST.JArray
import org.json4s.{JString, JValue}
import org.json4s.jackson.JsonMethods.{compact, parse}
import org.json4s.JsonDSL._
import org.scalatra.ScalatraServlet

trait PhonesRoutes extends ScalatraServlet with JSONProcessing {
  val db: Database

  get("/phones") {
    contentType = "application/json"
    response.setHeader("Access-Control-Allow-Origin", "*")

    getAllPhones
  }

  put("/phones/:id") {

    contentType = "application/json"
    response.setHeader("Access-Control-Allow-Origin", "*")

    val id = params("id") toInt
    val json = parse(request.body)

    val phone = getJsonValue[String]("phone", json)

    db withDynSession {
      phones insertOrUpdate(id, phone)
    }

    compact(
        ("id" -> id) ~
        ("phone" -> phone)
    )
  }

  post("/phones") {

    contentType = "application/json"
    response.setHeader("Access-Control-Allow-Origin", "*")

    val json = parse(request.body)
    val phone = json \ "phone" match {
      case JArray(l) => l
      case _ => throw new NotFoundJSONFieldException("phone")
    }

    db withDynSession {
      val jObj = phone.map {
        case JString(s) => {
          val id = (phones returning phones.map(_.id)) += (0, s)
            ("id" -> id) ~
            ("phone" -> s)
        }
        case _ => throw new NoSuchElementException("Invalid phones array")
      }
      compact(jObj)
    }
  }

  delete("/phones/:id") {

    contentType = "application/json"
    response.setHeader("Access-Control-Allow-Origin", "*")

    val id = params("id") toInt

    db withDynSession {
      phones filter(_.id === id) delete
    }

    getAllPhones
  }

  private def getAllPhones = {
    db withDynSession {

      val objList = phones.list map {
        case (id, phone) =>
            ("id" -> id) ~
            ("phone" -> phone)
      }

      compact(objList)
    }
  }

}
