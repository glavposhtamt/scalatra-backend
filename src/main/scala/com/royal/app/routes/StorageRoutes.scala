package com.royal.app.routes

import com.royal.app.JSONProcessing

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession
import com.royal.app.Tables._
import org.json4s.jackson.JsonMethods.{compact, parse}
import org.json4s.JsonDSL._
import org.scalatra.ScalatraServlet

trait StorageRoutes extends ScalatraServlet with JSONProcessing {
  val db: Database

  get("/storage/:key") {
    contentType = "application/json"
    response.setHeader("Access-Control-Allow-Origin", "*")

    val key = params("key")

    db withDynSession {
      val res: Seq[(String, String)] = storage filter(s => s.key === key) run

      if (res.isEmpty) throw new NoSuchElementException("Invalid key")
      else {
        val (_, value) = res(0)
        compact(("value" -> value))
      }
    }
  }

  put("/storage/:key") {

    contentType = "application/json"
    response.setHeader("Access-Control-Allow-Origin", "*")

    val key = params("key")
    val json = parse(request.body)

    val value = getJsonValue[String]("value", json)

    db withDynSession {
      storage insertOrUpdate(key, value)
    }

    compact(("value" -> value))
  }


  delete("/storage/:key") {

    contentType = "application/json"
    response.setHeader("Access-Control-Allow-Origin", "*")

    val key = params("key")

    db withDynSession {
      storage filter(_.key === key) delete
    }

    "{}"
  }
}
