package com.royal.app.routes

import com.royal.app.JSONProcessing
import com.royal.app.Tables._
import org.json4s.JsonAST
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods.{compact, _}
import org.scalatra._

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession
import scala.collection.mutable.{ArrayBuffer, Map => MMap}

trait CommentsRoutes extends ScalatraServlet with JSONProcessing{
  val db: Database

  get("/comments/:product") {

    contentType = "application/json"
    response.setHeader("Access-Control-Allow-Origin", "*")

    val id = params("product") toInt

    getComments(id)
  }

  put("/comments/:id") {

    contentType = "application/json"
    response.setHeader("Access-Control-Allow-Origin", "*")

    val id = params("id") toInt
    val comment = getCommentFromJSON(id, request.body)

    db withDynSession {
      comments += comment
      compact(
            ("id" -> comment.id) ~
            ("products_id" -> comment.productsId) ~
            ("author" -> comment.author) ~
            ("message" -> comment.message) ~
            ("date" -> comment.date) ~
            ("is_admin" -> comment.isAdmin) ~
            ("parent_id" -> comment.parentId)
      )
    }
  }

  post("/comments") {

    contentType = "application/json"
    response.setHeader("Access-Control-Allow-Origin", "*")

    val comment = getCommentFromJSON(0, request.body)

    db withDynSession {
      val id = (comments returning comments.map(_.id)) += comment
      compact(
          ("id" -> id) ~
          ("products_id" -> comment.productsId) ~
          ("author" -> comment.author) ~
          ("message" -> comment.message) ~
          ("date" -> comment.date) ~
          ("is_admin" -> comment.isAdmin) ~
          ("parent_id" -> comment.parentId)
      )
    }
  }

  delete("/comments/:id") {

    contentType = "application/json"
    response.setHeader("Access-Control-Allow-Origin", "*")

    val id = params("id") toInt

    db withDynSession {
      comments filter (_.id === id) delete

      comments filter (_.parentId === id) delete
    }

    "Success!"
  }


  private def getCommentFromJSON(id: Int, request: String): Comment = {
    val json = parse(request)

    Comment(id,
      getJsonValue[Int]("products_id", json),
      getJsonValue[String]("author", json),
      getJsonValue[String]("message", json),
      getJsonValue[String]("date", json),
      getJsonValue[Boolean]("is_admin", json),
      getJsonValue[Int]("parent_id", json)
    )
  }

  private def getCommentsTree(productId: Int) = {
    var commentsMap = MMap[Int, Comment]()
    var commentsTree = MMap[Int, (Comment, List[Comment])]()

    db withDynSession {
      comments.filter(_.productsId === productId).list.foreach(e => {
        commentsMap += (e.id -> e)
      })

      for ((k, v) <-commentsMap) {
        if (v.parentId > 0) {
          if (commentsTree.keys.exists(_ == v.parentId)) {
            commentsTree(v.parentId) = commentsTree(v.parentId) match {
              case (parent, child) => (parent, child :+ v)
            }
          }
          else  commentsTree += (v.parentId -> (commentsMap(v.parentId), List(v)))
        }
        else if (!commentsTree.keys.exists(_ == v.id)) commentsTree += (k -> (v, List()))
      }
    }

    commentsTree
  }

  def getComments(productId: Int): String = {
    val tree = getCommentsTree(productId)
    var commentsJson = ArrayBuffer[JsonAST.JObject]()

    for ((_, v) <- tree) {
      if (v._2.isEmpty)
        commentsJson +=
            ("id" -> v._1.id) ~
            ("products_id" -> v._1.productsId) ~
            ("author" -> v._1.author) ~
            ("message" -> v._1.message) ~
            ("date" -> v._1.date) ~
            ("is_admin" -> v._1.isAdmin)
      else
        commentsJson +=
             ("id" -> v._1.id) ~
             ("products_id" -> v._1.productsId) ~
             ("author" -> v._1.author) ~
             ("message" -> v._1.message) ~
             ("date" -> v._1.date) ~
             ("is_admin" -> v._1.isAdmin) ~
             ("children" ->  v._2.map(e => {
                     ("id" -> e.id) ~
                     ("products_id" -> e.productsId) ~
                     ("author" -> e.author) ~
                     ("message" -> e.message) ~
                     ("date" -> e.date) ~
                     ("is_admin" -> e.isAdmin)
             }))
      }

    compact(commentsJson)
  }
}
