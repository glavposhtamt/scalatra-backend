package com.royal.app.routes

import scala.collection.mutable.{Map => MMap}
import com.royal.app.Tables._
import com.royal.app.{ImageStorage, JSONProcessing, NotFoundJSONFieldException, ProductAPI}
import org.json4s.JValue
import org.json4s.JsonAST._
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods.{compact, parse}

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession
import org.scalatra.ScalatraServlet

trait ProductsRoutes extends ScalatraServlet with JSONProcessing with ImageStorage{
  val db: Database

  type Spec = (List[Int], List[Int], List[Int], List[Int], List[String])

  get("/product") {
    contentType = "application/json"
    response.setHeader("Access-Control-Allow-Origin", "*")

    val getParams: Seq[String] = Seq("from", "to", "category", "material", "brand", "price_max", "price_min", "color",
        "search", "sort_price", "selected", "id")
    var paramsMap = MMap[String, List[String]]()

    for (e <- getParams if !multiParams(e).isEmpty) paramsMap += (e -> multiParams(e).toList)

    db withDynSession {
      var q = ProductAPI.all
      var sorted = ""

      for ((key, value) <- paramsMap) {
        key match {
          case "id" => q = ProductAPI.id(q, value)
          case "selected" => q = ProductAPI.selected(q, value.head toBoolean)
          case "from" => q = ProductAPI.from(q, value.head toInt)
          case "to" => q = ProductAPI.to(q, value.head toInt)
          case "color" => q = ProductAPI.color(q, value)
          case "material" => q = ProductAPI.material(q, value)
          case "category" => q = ProductAPI.category(q, value)
          case "brand" => q = ProductAPI.brand(q, value.head toInt)
          case "search" => q = ProductAPI.search(q, value.head)
          case "price_max" => q = ProductAPI.priceMax(q, value.head toDouble)
          case "price_min" => q = ProductAPI.priceMin(q, value.head toDouble)
          case "sort_price" => sorted = value.head
          case _ => throw new NoSuchElementException("Wrong query")
        }
      }

      q = ProductAPI.group(q)
      if (sorted.length > 0) q = ProductAPI.order(q, sorted)

      val tp = ProductAPI.transpose(q.list)
      ProductAPI.toJSON(tp)
    }
  }

  get("/products/prices") {
    contentType = "application/json"
    response.setHeader("Access-Control-Allow-Origin", "*")

    db withDynSession {
      val price = products map(_.currentPrice)
      compact(("max" -> price.max.run) ~ ("min" -> price.min.run))
    }
  }

  put("/product/:id") {
    contentType = "application/json"
    response.setHeader("Access-Control-Allow-Origin", "*")

    val id = params("id")
    val json = parse(request.body)

    db withDynSession {
      products insertOrUpdate(initProduct(json, id toInt))
      setSpecs(id toInt, initSpecs(json))

      var q = ProductAPI.all
      q = ProductAPI.id(q, List(id))
      q = ProductAPI.group(q)
      val tp = ProductAPI.transpose(q.list)
      ProductAPI.toJSON(tp)
    }
  }

  post("/product") {
    contentType = "application/json"
    response.setHeader("Access-Control-Allow-Origin", "*")

    val json = parse(request.body)

    db withDynSession {
      val id = (products returning products.map(_.id)) += initProduct(json)

      try {
        setSpecs(id, initSpecs(json))
      } catch {
        case _: Throwable => {
          products filter(_.id === id) delete

          throw new NoSuchElementException("Not valid product metadata")
        }
      }

      var q = ProductAPI.all
      q = ProductAPI.id(q, List(id toString))
      q = ProductAPI.group(q)
      val tp = ProductAPI.transpose(q.list)
      ProductAPI.toJSON(tp)
    }
  }

  delete("/product/:id") {
    contentType = "text/plain"
    response.setHeader("Access-Control-Allow-Origin", "*")

    val id = params("id") toInt

    db withDynSession {
      products filter(_.id === id) delete

      var q = ProductAPI.all
      q = ProductAPI.group(q)
      val tp = ProductAPI.transpose(q.list)
      ProductAPI.toJSON(tp)
    }
  }

  def initProduct(json: JValue, id: Int = 0): Product = {
    val code = getJsonValue[String]("code", json)
    val title = getJsonValue[String]("title", json)
    val description = getJsonValue[String]("description", json)
    val size = getJsonValue[String]("size", json)
    val brandId = getJsonValue[String]("brand_id", json)
    val currentPrice = json \ "current_price" match {
      case JDouble(d) => d
      case JInt(d) => d toDouble
      case _ => throw new NotFoundJSONFieldException("current_price")
    }

    val oldPrice = json \ "old_price" match {
      case JDouble(d) => d
      case JInt(d) => d toDouble
      case _ => throw new NotFoundJSONFieldException("old_price")
    }

    val weight = getJsonValue[String]("weight", json)
    val total = getJsonValue[String]("total", json)
    val selected = getJsonValue[Boolean]("selected", json)

    Product(id, code, title, description, size, brandId toInt, currentPrice, oldPrice, weight, total toInt, selected)
  }

  def initSpecs(json: JValue): Spec = {
    val colorsId = json \ "colors_id" match {
      case JArray(l) => l
      case _ => throw new NotFoundJSONFieldException("colors_id")
    }

    val materialsId = json \ "materials_id" match {
      case JArray(l) => l
      case _ => throw new NotFoundJSONFieldException("materials_id")
    }

    val categoriesId = json \ "categories_id" match {
      case JArray(l) => l
      case _ => throw new NotFoundJSONFieldException("categories_id")
    }

    val citiesId = json \ "cities_id" match {
      case JArray(l) => l
      case _ => throw new NotFoundJSONFieldException("cities_id")
    }

    val base64 = json \ "images" match {
      case JArray(l) => l
      case _ => throw new NotFoundJSONFieldException("images")
    }

    (colorsId.map {
      case JInt(i) => i toInt
      case _ => throw new NoSuchElementException("Invalid value in field 'colors_id'") },

      materialsId.map {
        case JInt(i) => i toInt
        case _ => throw new NoSuchElementException("Invalid value in field 'materials_id'") },

      categoriesId.map {
        case JInt(i) => i toInt
        case _ => throw new NoSuchElementException("Invalid value in field 'categories_id'") },

      citiesId.map {
        case JInt(i) => i toInt
        case _ => throw new NoSuchElementException("Invalid value in field 'colors_id'") },

      base64.map {
        case JString(s) => s
        case _ => throw new NoSuchElementException("Invalid value in field 'images'")
      })
  }

  def setSpecs(productId: Int, spec: Spec): Unit = {
    val (col, mat, cat, cit, img) = spec

    if (col.length == 0 || cat.length == 0 || mat.length == 0 || cit.length == 0 || img.length == 0)
      throw new NoSuchElementException("Meta data not found")

    removeSpecs(productId)

    db withDynSession {
      col.map((e) => {
        colorsSpec insert (0, productId, e)
      })

      cat.map((e) => {
        categoriesSpec insert (0, productId, e)
      })

      mat.map((e) => {
        materialsSpec insert (0, productId, e)
      })

      cit.map((e) => {
        citiesSpec insert (0, productId, e)
      })

      img.map((e) => {
        val image = cloudinary(e)
        imagesSpec insert (0, productId, image)
      })
    }
  }

  def removeSpecs(productId: Int): Unit = {
    db withDynSession {
      colorsSpec filter(_.productsId === productId) delete

      imagesSpec filter(_.productsId === productId) delete

      categoriesSpec filter(_.productsId === productId) delete

      citiesSpec filter(_.productsId === productId) delete

      materialsSpec filter(_.productsId === productId) delete
    }
  }
}
