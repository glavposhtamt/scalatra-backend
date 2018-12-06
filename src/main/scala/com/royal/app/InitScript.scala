package com.royal.app

import org.scalatra._
import Tables.{imagesSpec, _}

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession


trait InitScript extends ScalatraServlet {
  val db: Database

  get("/init") {
    db withDynSession {
      // (colors.ddl ++ cities.ddl ++ phones.ddl ++ materials.ddl ++ storage.ddl ++
        (categories.ddl
        ++ brands.ddl ++ products.ddl ++ citiesSpec.ddl ++ colorsSpec.ddl ++ materialsSpec.ddl ++ categoriesSpec.ddl
        ++ imagesSpec.ddl ++ comments.ddl ++ users.ddl ++ orders.ddl).create
    }
    "Таблицы созданы"
  }

  get("/insert") {
    db withDynSession {
      // Insert some colors
      colors insertAll(
        Color(0, "white", "#FFFFFF"),
        Color(0, "black", "#000000"),
        Color(0, "red", "#FF0000"),
        Color(0, "lime", "#00FF00"),
        Color(0, "blue", "#0000FF"),
        Color(0, "yellow", "#FFFF00")
      )

      // Insert some cities
      cities insertAll(
        (0, "Moscow", 55.751244, 37.618423, "+79118667378"),
        (0, "Simferopol", 44.95719, 34.11079, "+79788667338"),
        (0, "Kiew", 50.45466, 30.5238, "+380973854615")
      )

      // Insert some materials
      materials insertAll(
        (0, "Дерево"),
        (0, "Стекло"),
        (0, "Глина"),
        (0, "Железо"),
        (0, "Говно"),
        (0, "Песок")
      )

      // Insert some categories
      categories insertAll(
        Widget(0, "Посуда", true, "sdfsfsfd"),
        Widget(0, "Вазы", true, "sdfsfsf")
      )

      // Insert some brands
      brands insertAll(
        Widget(0, "Scala", true, "sfsdf"),
        Widget(0, "Haskell", true, "sdfsff"),
        Widget(0, "PHP", false, "sdfsdfd")
      )

      // Insert some products
      products insertAll(
        Product(0, "aaabbb", "Phone", "Nokia C5", "12mm", 1, 12.0, 120.30, "12g", 8, true),
        Product(0, "cccddd", "Smartphone", "Strike", "12sm", 2, 100.50, 120.50, "16g", 10, false),
        Product(0, "eeefff", "Product", "Bla-bla", "14sm", 3, 100.50, 120.50, "16g", 10, true)
      )

      // Insert colors_spec
      colorsSpec insertAll(
        (0, 1, 1),
        (0, 1, 2),
        (0, 2, 1),
        (0, 2, 3),
        (0, 2, 4),
        (0, 3, 4),
        (0, 3, 5)
      )

      // Insert materials_spec
      materialsSpec insertAll(
        (0, 1, 1),
        (0, 1, 2),
        (0, 2, 1),
        (0, 2, 3),
        (0, 2, 4),
        (0, 3, 4),
        (0, 3, 5)
      )

      // Insert categories_spec
      categoriesSpec insertAll(
        (0, 1, 1),
        (0, 1, 2),
        (0, 2, 1),
        (0, 3, 2)
      )

      citiesSpec insertAll(
        (0, 1, 1),
        (0, 1, 2),
        (0, 1, 3),
        (0, 2, 2),
        (0, 2, 3),
        (0, 3, 1)
      )

      imagesSpec insertAll(
        (0, 1, "sdfsdf"),
        (0, 1, "sdfsdfd"),
        (0, 2, "sfsdf"),
        (0, 2, "ssfsdf"),
        (0, 3, "sdfsfsf")
      )
    }
    "Данные записаны"
  }

}
