package com.royal.app

import org.json4s.{DefaultFormats, Formats, JValue}

trait JSONProcessing {
  protected implicit lazy val jsonFormats: Formats = DefaultFormats

  def getJsonValue[T: Manifest](key: String, json: JValue): T = {
    try {
      (json \ key).extract[T]
    } catch {
      case _: Throwable => throw new NotFoundJSONFieldException(key)
    }
  }
}
