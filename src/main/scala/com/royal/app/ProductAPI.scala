package com.royal.app

import com.royal.app.Tables.{Widget, Product}
import org.json4s.jackson.JsonMethods.compact
import org.json4s.JsonDSL._

import scala.slick.jdbc.{GetResult, StaticQuery => Q}
import Q.interpolation

object ProductAPI {

  type Data = List[List[String]]
  type Query = Q[Unit, ProductsData]

  case class ProductsData(product: Product, brand: Widget, category: Data,
                          color: Data, material: Data, city: Data, image: Data)

  implicit val getProductsDataResult = GetResult(r => {
    ProductsData(
      Product(r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<),
      Widget(r.<<, r.<<, r.<<, r.<<),
      List(List(r.<<[String], r.<<[String], r.<<[String])),
      List(List(r.<<[String], r.<<[String], r.<<[String])),
      List(List(r.<<[String], r.<<[String])),
      List(List(r.<<[String], r.<<[String], r.<<[String], r.<<[String], r.<<[String])),
      List(List(r.<<[String])))
  })

  def all: Query =
    sql"""
          SELECT p.*, b.id as brand_id, b.title as brand_title, b.selected as brand_selected, b.image as brand_image,
            group_concat(DISTINCT cat.id) as categories_ids,
            group_concat(DISTINCT cat.title) as categories_titles,
            group_concat(DISTINCT cat.image) as category_images,
            group_concat(DISTINCT col.id) as colors_ids,
            group_concat(DISTINCT col.title) as colors_titles,
            group_concat(DISTINCT col.hex) as hexes,
            group_concat(DISTINCT m.id) as materials_ids,
            group_concat(DISTINCT m.title) as materials_titles,
            group_concat(DISTINCT c.id) as cities_ids,
            group_concat(DISTINCT c.name) as cities_names,
            group_concat(DISTINCT c.latitude) as cities_latitudes,
            group_concat(DISTINCT c.longitude) as cities_longitudes,
            group_concat(DISTINCT c.phone) as cities_phones,
            group_concat(DISTINCT img.image) as product_images
          FROM products p
            JOIN brands b ON b.id = p.brand_id
            LEFT JOIN colors_spec cs ON cs.products_id = p.id
            LEFT JOIN colors col ON col.id = cs.colors_id
            LEFT JOIN materials_spec ms ON ms.products_id = p.id
            LEFT JOIN materials m ON m.id = ms.materials_id
            LEFT JOIN categories_spec cats ON cats.products_id = p.id
            LEFT JOIN categories cat ON cat.id = cats.categories_id
            LEFT JOIN cities_spec citc ON citc.products_id = p.id
            LEFT JOIN cities c ON c.id = citc.cities_id
            LEFT JOIN images_spec img ON img.products_id = p.id
          WHERE TRUE
    """.as[ProductsData]

  def id(query: Query, ids: List[String]): Query = {
    if (ids.length == 1) query + " AND p.id = " +? ids.head.toInt
    else {
      val idsInt = ids.map(_.toInt)
      query + s" AND p.id IN(${idsInt.mkString(",")})"
    }
  }

  def selected(query: Query, selected: Boolean): Query = query + " AND p.selected = " +? selected

  def from(query: Query, id: Int): Query = query + " AND p.id >= " +? id

  def to(query: Query, id: Int): Query = query + " AND p.id < " +? id

  def color(query: Query, ids: List[String]): Query = {
    if (ids.length == 1) query + " AND col.id = " +? ids.head.toInt
    else {
      val idsInt = ids.map(_.toInt)
      query + s" AND col.id IN(${idsInt.mkString(",")})"
    }
  }

  def material(query: Query,  ids: List[String]): Query = {
    if (ids.length == 1) query + " AND m.id = " +? ids.head.toInt
    else {
      val idsInt = ids.map(_.toInt)
      query + s" AND m.id IN(${idsInt.mkString(",")})"
    }
  }

  def category(query: Query, ids: List[String]): Query = {
    if (ids.length == 1) query + " AND cat.id = " +? ids.head.toInt
    else {
      val idsInt = ids.map(_.toInt)
      query + s" AND cat.id IN(${idsInt.mkString(",")})"
    }
  }

  def brand(query: Query, id: Int): Query = query + " AND b.id = " +? id

  def priceMax(query: Query, price: Double): Query = query + " AND p.current_price <= " +? price

  def priceMin(query: Query, price: Double): Query = query + " AND p.current_price >= " +? price

  def search(query: Query, value: String): Query = query + s" AND p.title LIKE '%${value}%'"

  def group(query: Query): Query = query + " GROUP BY p.id"

  def order(query: Query, flag: String): Query = flag match {
    case "up" => query + " ORDER BY p.current_price DESC"
    case "down" => query + " ORDER BY p.current_price ASC"
    case _ => throw new IllegalArgumentException("Invalid sorting flag")
  }

  def transpose(data: List[ProductsData]): List[ProductsData] =
    try {
      data.map(e => {
        ProductsData(
          e.product,
          e.brand,
          e.category.head.map(s => s.split(",")).transpose,
          e.color.head.map(s => s.split(",")).transpose,
          e.material.head.map(s => s.split(",")).transpose,
          e.city.head.map(s => s.split(",")).transpose,
          e.image.head.map(s => s.split(",")).transpose
        )
      })
    } catch {
      case _: NullPointerException => throw new NoSuchElementException("Incomplete product data")
    }

  def toJSON(list: List[ProductsData]): String = {
    compact(list map {
      case ProductsData(p, b, cat, col, mat, cit, img) => {
          ("id" -> p.id) ~
          ("code" -> p.code) ~
          ("title" -> p.title) ~
          ("description" -> p.description) ~
          ("size" -> p.size) ~
          ("current_price" -> p.currentPrice) ~
          ("old_price" -> p.oldPrice) ~
          ("weight" -> p.weight) ~
          ("total" -> p.total) ~
          ("selected" -> p.selected) ~
            ("brand" -> (
              ("id" -> b.id) ~
              ("title" -> b.title) ~
              ("selected" -> b.selected) ~
              ("image" -> b.image)
            )) ~
          ("category" -> cat.map(e => {
              ("id" -> e.head) ~
              ("title" -> e(1)) ~
              ("image" -> e(2))
          })) ~
        ("color" -> col.map(e => {
              ("id" -> e.head) ~
              ("title" -> e(1)) ~
              ("hex" -> e(2))
        })) ~
        ("material" -> mat.map(e => {
              ("id" -> e.head) ~
              ("title" -> e(1))
        })) ~
        ("city" -> cit.map(e => {
              ("id" -> e.head) ~
              ("name" -> e(1)) ~
              ("latitude" -> e(2)) ~
              ("longitude" -> e(3)) ~
              ("phone" -> e(4))
        })) ~
        ("images" -> img.map {
          case List(img) => img
        })
      }
    })
  }
}
