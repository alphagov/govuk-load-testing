package govuk

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import scala.util.Random

class Frontend extends Simulation {
  val baseUrl = sys.props.get("baseUrl").get
  val username = sys.props.get("username").get
  val password = sys.props.get("password").get
  val rateLimitToken = sys.props.get("rateLimitToken")
  val users = sys.props.getOrElse("users", "1").toInt
  val ramp = sys.props.getOrElse("ramp", "0").toInt
  val bust = sys.props.getOrElse("bust", "false").toBoolean

  val cachebuster = Iterator.continually(
    Map("cachebust" -> (Random.alphanumeric.take(50).mkString)))

  val extraHeaders = Map(
    "Rate-Limit-Token" -> rateLimitToken
  ).collect {
    case (key, Some(value)) => key -> value
  }

  val httpProtocol = http
    .baseUrl(baseUrl)
    .acceptHeader("text/html")
    .acceptEncodingHeader("gzip, deflate")
    .basicAuth(username, password)
    .headers(extraHeaders)

  val frontend = scenario("Frontend")
    .feed(cachebuster)
    .exec(
      http("homepage")
        .get(if (bust) "/?cachebust=${cachebust}" else "/")
        .check(
          status.in(200 to 299),
          regex("govuk:rendering-application").count.is(1),
          regex("govuk:content-id").count.is(1)
        ))

  setUp(
    frontend.inject(rampUsers(users) during (ramp seconds))
  ).protocols(httpProtocol)
}
