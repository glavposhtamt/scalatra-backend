package com.royal.app.routes


import com.royal.app.Tables._
import com.royal.app.{JSONProcessing, NotFoundJSONFieldException}
import org.json4s.JsonAST._
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods.{compact, parse}

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession
import org.scalatra.ScalatraServlet

trait OrdersRoutes extends ScalatraServlet with JSONProcessing {
  val db: Database

  get("/orders") {
    contentType = "application/json"
    response.setHeader("Access-Control-Allow-Origin", "*")

    db withDynSession {
      val data = ordersAPI.get list

      val objJson = data map {
        case OrdersData(user, products, total) => {
          val prods = products.head.split(",").toList map(_.toInt)
          val count = total.head.split(",").toList map(_.toInt)
          val data = prods.zip(count)

          ("order" -> user.id) ~
          ("full_name" -> user.fullName) ~
          ("address" -> user.address) ~
          ("email" -> user.email) ~
          ("tel" -> user.tel) ~
          ("subscribe" -> user.subscribe) ~
          ("is_read" -> user.isRead) ~
          ("products" -> data.map {
            case (p, t) => ("product_id" -> p) ~ ("total" -> t)
          })
        }
      }
      compact(objJson)
    }
  }

  get("/orders/new") {
    contentType = "application/json"
    response.setHeader("Access-Control-Allow-Origin", "*")

    db withDynSession {
      val count = users.filter(_.isRead === false).length
      compact(("length" -> count.run))
    }
  }

  get("/orders/email") {
    contentType = "application/json"
    response.setHeader("Access-Control-Allow-Origin", "*")

    db withDynSession {
      val email = users map(_.email)
      compact(("email" -> email.list))
    }
  }

  put("/orders/:id") {
    contentType = "application/json"
    response.setHeader("Access-Control-Allow-Origin", "*")

    val id = params("id") toInt

    db withDynSession {
      val q = for { c <- users if c.id === id } yield c.isRead
      q.update(true)
    }
    compact(("order" -> id))
  }

  post("/orders") {
    contentType = "application/json"
    response.setHeader("Access-Control-Allow-Origin", "*")

    val (user, order) = getOrdersInfo(request.body)

    db withDynSession {
      val orderId = (users returning users.map(_.id)) += user

      order map {
        case (p, t) =>
        orders insertOrUpdate (0, p, orderId, t)
      }
      compact(("order" -> orderId))
    }
  }

  private def getOrdersInfo(request: String): (User, List[(Int, Int)]) = {
    val json = parse(request)

    val productsId = json \ "products" match {
      case JArray(a) => a
      case _ => throw new NotFoundJSONFieldException("products")
    }

    val data = productsId.map {
      case JObject(obj) => {
        obj match {
          case List(("products_id", JInt(p)), ("total", JInt(t))) => (p toInt, t toInt)
          case List(("total", JInt(t)), ("products_id", JInt(p))) => (p toInt, t toInt)
          case _ => throw new NoSuchElementException("Invalid object in field 'products'")
        }
      }
      case _ => throw new NoSuchElementException("Invalid values in field 'products'") }

    (User(0,
      getJsonValue[String]("full_name", json),
      getJsonValue[String]("address", json),
      getJsonValue[String]("email", json),
      getJsonValue[String]("tel", json),
      getJsonValue[Boolean]("subscribe", json),
      false), data
    )
  }
}
