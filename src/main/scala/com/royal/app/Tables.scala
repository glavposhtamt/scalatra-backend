package com.royal.app

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.{GetResult, StaticQuery => Q}
import Q.interpolation

object Tables {

  // Definition of the Colors table
  case class Color(id: Int, title: String, hex: String)
  class Colors(tag: Tag) extends Table[Color](tag, "colors") {
    def id      = column[Int]("id", O.PrimaryKey, O.AutoInc) // This is the primary key column
    def title    = column[String]("title")
    def hex  = column[String]("hex")


    // Every table needs a * projection with the same type as the table's type parameter
    def * = (id, title, hex) <> (Color.tupled, Color.unapply)
    def idxTitle = index("IDX_UNIQUE_COLOR_TITLE", title, unique = true)
    def idxHex = index("IDX_UNIQUE_COLOR_HEX", hex, unique = true)

  }
  val colors = TableQuery[Colors]

  // Definition of the Cities table
  class Cities(tag: Tag) extends Table[(Int, String, Double, Double, String)](tag, "cities") {
    def id      = column[Int]("id", O.PrimaryKey, O.AutoInc) // This is the primary key column
    def name    = column[String]("name")
    def latitude  = column[Double]("latitude")
    def longitude = column[Double]("longitude")
    def phone = column[String]("phone")

    def idxName = index("IDX_UNIQUE_CITY_NAME", name, unique = true)
    def idxPhone = index("IDX_UNIQUE_CITY_PHONE", phone, unique = true)
    def idxCoordinates = index("IDX_UNIQUE_CITY_COORDINATES", (latitude, longitude), unique = true)

    // Every table needs a * projection with the same type as the table's type parameter
    def * = (id, name, latitude, longitude, phone)

  }
  val cities = TableQuery[Cities]

  // Definition of the Phones table
  class Phones(tag: Tag) extends Table[(Int, String)](tag, "phones") {
    def id= column[Int]("id", O.PrimaryKey, O.AutoInc) // This is the primary key column
    def phone= column[String]("contact")

    // Every table needs a * projection with the same type as the table's type parameter
    def * = (id, phone)
    def idx = index("IDX_UNIQUE_PHONES", phone, unique = true)
  }
  val phones = TableQuery[Phones]

  // Definition of the Material table
  class Materials(tag: Tag) extends Table[(Int, String)](tag, "materials") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc) // This is the primary key column
    def title= column[String]("title")

    def idx = index("IDX_UNIQUE_MATERIAL", title, unique = true)
    // Every table needs a * projection with the same type as the table's type parameter
    def * = (id, title)

  }
  val materials = TableQuery[Materials]

  // Definition of the Storage table
  class Storage(tag: Tag) extends Table[(String, String)](tag, "storage") {
    def key      = column[String]("key") // This is the unique key column
    def value   = column[String]("value", O DBType "varchar(2048)")

    // Every table needs a * projection with the same type as the table's type parameter
    def * = (key, value)

    def idx = index("IDX_UNIQUE_STORAGE", key, unique = true)

  }
  val storage = TableQuery[Storage]

  // Definition of the Categories table
  case class Widget(id: Int, title: String, selected: Boolean, image: String)
  class Categories(tag: Tag) extends Table[Widget](tag, "categories") {
    def id      = column[Int]("id", O.PrimaryKey, O.AutoInc) // This is the primary key column
    def title   = column[String]("title")
    def selected = column[Boolean]("selected")
    def image   = column[String]("image")

    // Every table needs a * projection with the same type as the table's type parameter
    def * = (id, title, selected, image) <> (Widget.tupled, Widget.unapply)
    def idx = index("IDX_UNIQUE_CATEGORY", title, unique = true)
  }
  val categories = TableQuery[Categories]

  // Definition of the Brands table
  class Brands(tag: Tag) extends Table[Widget](tag, "brands") {
    def id      = column[Int]("id", O.PrimaryKey, O.AutoInc) // This is the primary key column
    def title   = column[String]("title")
    def selected = column[Boolean]("selected")
    def image   = column[String]("image")

    // Every table needs a * projection with the same type as the table's type parameter
    def * = (id, title, selected, image) <> (Widget.tupled, Widget.unapply)
    def idx = index("IDX_UNIQUE_BRAND", title, unique = true)
  }
  val brands = TableQuery[Brands]


  // Definition of the Products table
  case class Product(id: Int, code: String, title: String, description: String, size: String, brandId: Int,
                     currentPrice: Double, oldPrice: Double, weight: String, total: Int, selected: Boolean)
  class Products(tag: Tag) extends Table[Product](tag, "products") {

    def id = column[Int]("id", O.PrimaryKey, O.AutoInc) // This is the primary key column
    def code= column[String]("code")
    def title= column[String]("title")
    def description = column[String]("description", O DBType "text")
    def size= column[String]("size")
    def brandId= column[Int]("brand_id")
    def currentPrice= column[Double]("current_price")
    def oldPrice = column[Double]("old_price")
    def weight = column[String]("weight")
    def total = column[Int]("total")
    def selected = column[Boolean]("selected")

    // Every table needs a * projection with the same type as the table's type parameter
    def * = (id, code, title, description, size, brandId, currentPrice,
                                    oldPrice, weight, total, selected) <> (Product.tupled, Product.unapply)
    def brand = foreignKey("BRAND_FK", brandId, brands)(_.id)
    def idx = index("IDX_UNIQUE_CODE", code, unique = true)

  }
  val products = TableQuery[Products]

  // Definition of the CategoriesSpec table
  class CategoriesSpec(tag: Tag) extends Table[(Int, Int, Int)](tag, "categories_spec") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc) // This is the primary key column
    def productsId = column[Int]("products_id")
    def categoriesId   = column[Int]("categories_id")

    // Every table needs a * projection with the same type as the table's type parameter
    def * = (id, productsId, categoriesId)

    def product = foreignKey("CAT_PRODUCT_FK", productsId, products)(_.id,
      onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def category = foreignKey("CATEGORY_FK", categoriesId, categories)(_.id)
  }
  val categoriesSpec = TableQuery[CategoriesSpec]

  // Definition of the MaterialsSpec table
  class MaterialsSpec(tag: Tag) extends Table[(Int, Int, Int)](tag, "materials_spec") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc) // This is the primary key column
    def productsId = column[Int]("products_id")
    def materialsId   = column[Int]("materials_id")

    // Every table needs a * projection with the same type as the table's type parameter
    def * = (id, productsId, materialsId)

    def product = foreignKey("MAT_PRODUCT_FK", productsId, products)(_.id,
      onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def material = foreignKey("MATERIAL_FK", materialsId, materials)(_.id)
  }
  val materialsSpec = TableQuery[MaterialsSpec]

  // Definition of the ColorsSpec table
  class ColorsSpec(tag: Tag) extends Table[(Int, Int, Int)](tag, "colors_spec") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc) // This is the primary key column
    def productsId = column[Int]("products_id")
    def colorsId   = column[Int]("colors_id")

    // Every table needs a * projection with the same type as the table's type parameter
    def * = (id, productsId, colorsId)

    def product = foreignKey("COL_PRODUCT_FK", productsId, products)(_.id,
      onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def color = foreignKey("COLORS_FK", colorsId, colors)(_.id)
  }
  val colorsSpec = TableQuery[ColorsSpec]

  // Definition of the CitiesSpec table
  class CitiesSpec(tag: Tag) extends Table[(Int, Int, Int)](tag, "cities_spec") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc) // This is the primary key column
    def productsId = column[Int]("products_id")
    def citiesId   = column[Int]("cities_id")

    // Every table needs a * projection with the same type as the table's type parameter
    def * = (id, productsId, citiesId)

    def product = foreignKey("CIT_PRODUCT_FK", productsId, products)(_.id,
      onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def city = foreignKey("CITIES_FK", citiesId, cities)(_.id)
  }
  val citiesSpec = TableQuery[CitiesSpec]

  // Definition of the ImagesSpec table
  class ImagesSpec(tag: Tag) extends Table[(Int, Int, String)](tag, "images_spec") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc) // This is the primary key column
    def productsId = column[Int]("products_id")
    def image   = column[String]("image")

    // Every table needs a * projection with the same type as the table's type parameter
    def * = (id, productsId, image)

    def product = foreignKey("IMG_PRODUCT_FK", productsId, products)(_.id,
      onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
  }
  val imagesSpec = TableQuery[ImagesSpec]

  // Definition of the Comments table
  case class Comment(id: Int, productsId: Int, author: String, message: String, date: String, isAdmin: Boolean,
                     parentId: Int)
  class Comments(tag: Tag) extends Table[Comment](tag, "comments") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc) // This is the primary key column
    def productsId = column[Int]("products_id")
    def author   = column[String]("author")
    def message = column[String]("message", O DBType "text")
    def date = column[String]("date")
    def isAdmin = column[Boolean]("is_admin")
    def parentId = column[Int]("parent_id", O.Default(0))

    // Every table needs a * projection with the same type as the table's type parameter
    def * = (id, productsId, author, message, date, isAdmin, parentId) <>
            (Comment.tupled, Comment.unapply)

    def product = foreignKey("COMMENTS_PRODUCT_FK", productsId, products)(_.id,
      onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
  }
  val comments = TableQuery[Comments]

  case class User(id: Int, fullName: String, address: String, email: String, tel: String, subscribe: Boolean,
                  isRead: Boolean)
  class Users(tag: Tag) extends Table[User](tag, "users") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc) // This is the primary key column
    def fullName = column[String]("full_name")
    def address = column[String]("address")
    def email = column[String]("email")
    def tel = column[String]("tel")
    def subscribe = column[Boolean]("subscribe")
    def isRead = column[Boolean]("is_read", O.Default(false))

    def * = (id, fullName, address, email, tel, subscribe, isRead) <> (User.tupled, User.unapply)
  }

  val users = TableQuery[Users]
  case class OrdersData(user: User, products: List[String], total: List[String])
  class Orders(tag: Tag) extends Table[(Int, Int, Int, Int)](tag, "orders") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc) // This is the primary key column
    def productsId = column[Int]("products_id")
    def userId   = column[Int]("user_id")
    def total   = column[Int]("total")

    def * = (id, productsId, userId, total)
    def product = foreignKey("ORDER_PRODUCT_FK", productsId, products)(_.id,
      onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def user = foreignKey("USER_FK", userId, users)(_.id,
      onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
  }

  object ordersAPI {
    type Query = Q[Unit, OrdersData]

    implicit val getOrdersDataResult = GetResult(r => {
      OrdersData(
        User(r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<),
        List(r.<<[String]), List(r.<<[String]))
    })

    def get: Query =
      sql"""
           SELECT u.*, group_concat(o.products_id) as products,
           			group_concat(o.total) as total FROM users u
           JOIN orders o ON o.user_id = u.id GROUP BY u.id;
    """.as[OrdersData]
  }

  val orders = TableQuery[Orders]
}