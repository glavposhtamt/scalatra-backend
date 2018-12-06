val ScalatraVersion = "2.6.3"

organization := "com.royal"

name := "royal"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.12.6"

resolvers += Classpaths.typesafeReleases

libraryDependencies ++= Seq(
  "org.scalatra" %% "scalatra" % ScalatraVersion,
  "org.scalatra" %% "scalatra-scalatest" % ScalatraVersion % "test",
  "com.jason-goodwin" %% "authentikat-jwt" % "0.4.5",
  "ch.qos.logback" % "logback-classic" % "1.2.3" % "runtime",
//  "org.eclipse.jetty" % "jetty-server" % "9.4.12.v20180830",
  "org.eclipse.jetty" % "jetty-webapp" % "9.4.9.v20180320" % "compile;container",
  "org.eclipse.jetty" % "jetty-plus" % "9.4.9.v20180320" % "compile;container",
  "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided",
  "org.json4s" %% "json4s-native" % "3.6.1",
  "org.json4s" %% "json4s-jackson" % "3.6.1",
  "com.typesafe.slick" %% "slick" % "2.1.0",
  "com.cloudinary" % "cloudinary-http44" % "1.21.0",
  "mysql" % "mysql-connector-java" % "8.0.12",
  "c3p0" % "c3p0" % "0.9.1.2",
)


enablePlugins(ScalatraPlugin)
enablePlugins(JavaAppPackaging)

