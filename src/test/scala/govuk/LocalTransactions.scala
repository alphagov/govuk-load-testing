package govuk

import io.gatling.core.Predef._
import io.gatling.http.Predef._

class LocalTransactions extends Simulation {
  val postcodes = csv(dataDir + java.io.File.separatorChar + "all_postcodes.csv.gz").unzip.eager.random

  val duration = sys.props.getOrElse("duration", "3600").toInt

  val scn = scenario("Load test")
      .feed(postcodes)
      .during(duration, "Soak test") {
        exec(http("Check postcode")
            .post("/check-school-closure")
            .formParam("postcode", "${postcode}")
            .disableFollowRedirect
            .check(status.is(302)))
        .pause(1)
      }

  run(scn)
}
