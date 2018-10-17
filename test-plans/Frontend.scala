package govuk

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class Frontend extends Simulation {
  val baseUrl = System.getProperty("baseurl")
  val username = System.getProperty("username")
  val password = System.getProperty("password")
  val users = Integer.getInteger("users", 1)
  val ramp = Integer.getInteger("ramp", 0)

  val httpProtocol = http
    .baseUrl(baseUrl)
    .acceptHeader("text/html")
    .acceptEncodingHeader("gzip, deflate")
    .basicAuth(username, password)

  val frontend = scenario("Frontend")
    .exec(http("homepage").get("/"))

  setUp(
    frontend.inject(rampUsers(users) during (ramp seconds))
  ).protocols(httpProtocol)
}
