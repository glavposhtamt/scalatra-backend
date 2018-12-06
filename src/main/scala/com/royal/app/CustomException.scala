package com.royal.app

final class NotFoundJSONFieldException(var message: String) extends Exception(message) {
  override def getMessage: String = notFoundFieldMessage(super.getMessage)

  def notFoundFieldMessage(title: String): String = s"Field '${title}' is not a found"
}

final case class NotAuthException(var message: String) extends Exception(message)
final case class NotFoundException(var message: String) extends Exception(message)