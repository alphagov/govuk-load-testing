package govuk

import io.gatling.core.Predef._
import io.gatling.http.Predef._

class SchoolClosure extends Simulation {
  val duration = sys.props.getOrElse("duration", "3600").toInt

  val scn = scenario("Test school closure post requests")
    .during(duration, "Soak test") {
      exec(
        http("check school closures")
          .post("/check-school-closure")
          .formParam("postcode", "SW1A 2AA")
          .disableFollowRedirect
          .check(status.is(302))
      )
    }

  run(scn)
}
