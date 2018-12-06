import com.royal.app._
import javax.servlet.ServletContext
import org.scalatra._

import com.mchange.v2.c3p0.ComboPooledDataSource
import org.slf4j.LoggerFactory
import scala.slick.jdbc.JdbcBackend.Database
import org.scalatra.servlet.MultipartConfig

class ScalatraBootstrap extends LifeCycle {

  val logger = LoggerFactory.getLogger(getClass)
  val cpds = new ComboPooledDataSource
  logger.info("Created c3p0 connection pool")


  override def init(context: ServletContext) {
    val db = Database.forDataSource(cpds)  // create a Database which uses the DataSource

    context.mount(RoyalApp(db), "/api/*")
    context.initParameters("org.scalatra.cors.allowedOrigins") = "https://royaldeco.herokuapp.com,http://localhost:8080"
    context.initParameters("org.scalatra.cors.allowedMethods") = "GET,POST,PUT,OPTIONS,DELETE"
    context.initParameters("org.scalatra.cors.allowedHeaders") = "Content-Type,Content-Length,X-Requested-With"
  }

  private def closeDbConnection() {
    logger.info("Closing c3po connection pool")
    cpds.close
  }

  override def destroy(context: ServletContext) {
    super.destroy(context)
    closeDbConnection
  }
}

