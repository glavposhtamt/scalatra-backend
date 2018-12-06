package com.royal.app

import org.scalatra._
import org.scalatra.servlet.SizeConstraintExceededException

import scala.slick.driver.MySQLDriver.simple._
import java.nio.file.NoSuchFileException

import com.royal.app.routes._


case class RoyalApp(db: Database) extends ScalatraServlet with ColorsRoutes with InitScript with CitiesRoutes
  with PhonesRoutes with MaterialRoutes with StorageRoutes with CategoriesRoutes
  with BrandsRoutes with ProductsRoutes with CommentsRoutes with OrdersRoutes with Auth with CorsSupport {

  error {
    case e: NotFoundJSONFieldException => {
      contentType = "text/plain"
      BadRequest(e getMessage)
    }
    case e: NotAuthException => {
      contentType = "text/plain"
      Forbidden(e getMessage)
    }
    case e: NotFoundException => {
      contentType = "text/plain"
      NotFound(e getMessage)
    }
    case e: NoSuchElementException => {
      contentType = "text/plain"
      BadRequest(e getMessage)
    }
    case e: NoSuchFileException => NotFound(s"File ${e getMessage} not found")
    case _: java.lang.NumberFormatException => BadRequest("Invalid number")
    case _: java.lang.IllegalArgumentException => BadRequest("Invalid value")
    case _: SizeConstraintExceededException => RequestEntityTooLarge("too much!")
    case e: java.sql.SQLIntegrityConstraintViolationException => {
      contentType = "text/plain"

      val error: String = e getMessage

      if (error.indexOf("BRAND_FK") > 0) {
        BadRequest("В продаже имеются продукты этого бренда.")
      } else BadRequest(error)
    }
    case _: Throwable => BadRequest("Не удалось загрузить данные из-за медленного соединения.")
  }

  options("/*") {
    response setHeader("Access-Control-Allow-Headers", request.getHeader("Access-Control-Request-Headers"))
  }

}