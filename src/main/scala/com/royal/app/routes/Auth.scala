package com.royal.app.routes

import com.royal.app.JSONProcessing
import authentikat.jwt.{JsonWebToken, JwtClaimsSet, JwtHeader}
import com.royal.app.NotAuthException
import org.json4s.jackson.JsonMethods.{compact, parse}
import org.json4s.JsonDSL._
import org.scalatra.ScalatraServlet

trait Auth extends ScalatraServlet with JSONProcessing{

  val user = "admin"
  val password = "entry-royal"
//
//  before("/*") {
//    response.setHeader("Access-Control-Allow-Origin", "*")
//
//    if (request.getPathInfo.indexOf("/me") == -1 && request.getPathInfo.indexOf("/comments") == -1 &&
//        request.getMethod != "GET") {
//
//      val jwt = request.header("X-Auth-Token") match {
//        case Some(token) => token
//        case None => throw new NotAuthException("Отправь JWT-токен в заголовке, чтобы можно было работать с админкой")
//      }
//
//      if (jwt != token(user, password))
//        throw new NotAuthException("Невалидный JWT-токен")
//    }
//  }

  post("/me") {
    contentType = "application/json"
    response.setHeader("Access-Control-Allow-Origin", "*")

    val json = parse(request.body)
    val customer = getJsonValue[String]("user", json)
    val pass = getJsonValue[String]("password", json)

    if (customer != user || pass != password) throw new NotAuthException("Неверный логин или пароль")
    else {
      val jwt = token(customer, pass)
      compact(("token" -> jwt))
    }
  }

  def token(user: String, password: String): String = {
    val header = JwtHeader("HS256")
    val claimsSet = JwtClaimsSet(Map("user" -> user, "password" -> password))
    JsonWebToken(header, claimsSet, "secret")
  }
}
