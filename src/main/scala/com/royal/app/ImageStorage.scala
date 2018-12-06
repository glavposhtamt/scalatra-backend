package com.royal.app

import java.util.HashMap
import com.cloudinary.Cloudinary

trait ImageStorage {
  def cloudinary(base64: String) = {

    val config: HashMap[String, String] = new HashMap()
    config.put("cloud_name", "secret")
    config.put("api_key", "secret")
    config.put("api_secret", "secret")

    val cloudinary = new Cloudinary(config)

    val params: HashMap[String, String] = new HashMap()
    params.put("public_id", System.currentTimeMillis.toString)
    params.put("overwrite", "true")
    params.put("notification_url", "https://requestb.in/12345abcd")
    params.put("resource_type", "image")

    val imageData = cloudinary.uploader().upload(base64, params)
    println(imageData)
    imageData.get("url").toString
  }
}
