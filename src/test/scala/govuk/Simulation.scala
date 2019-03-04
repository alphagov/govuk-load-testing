package govuk

import io.gatling.core.Predef._
import io.gatling.core.scenario
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.HeaderNames
import io.gatling.http.Predef._
import scala.concurrent.duration._
import scala.util.Random

abstract class Simulation extends scenario.Simulation {
  val dataDir = sys.props.getOrElse("dataDir", "src/test/resources/test-data")
  val baseUrl = sys.props.get("baseUrl").get
  val username = sys.props.getOrElse("username", "")
  val password = sys.props.getOrElse("password", "")
  val rateLimitToken = sys.props.get("rateLimitToken")
  val workers = sys.props.getOrElse("workers", "1").toInt
  val ramp = sys.props.getOrElse("ramp", "0").toInt
  val bust = sys.props.getOrElse("bust", "false").toBoolean

  val cachebuster = Iterator.continually(
    Map("cachebust" -> (Random.alphanumeric.take(50).mkString)))

  val extraHeaders = Map(
    "Rate-Limit-Token" -> rateLimitToken
  ).collect {
    case (key, Some(value)) => key -> value
  }

  var httpProtocol = http
    .baseUrl(baseUrl)
    .acceptHeader("text/html")
    .acceptEncodingHeader("gzip, deflate")
    .headers(extraHeaders)
    .disableCaching

  if (!username.isEmpty && !password.isEmpty) {
    httpProtocol = httpProtocol.basicAuth(username, password)
  }

  def get(path: String, cachebust: String) =
    http(path)
      .get(path + (if (bust) "?cachebust=" + cachebust else ""))
      .check(header(HeaderNames.ContentType).saveAs("content-type"))
      .check(
        status.in(200 to 299),
        checkIf(session => {
          session("content-type").as[String] contains "text/html"
        })(regex("govuk:rendering-application").count.is(1))
      )

  def run(scn: ScenarioBuilder) =
    setUp(
      scn.inject(rampUsers(workers) during (ramp seconds))
    ).protocols(httpProtocol)
}
