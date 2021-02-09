package govuk

import io.gatling.core.Predef._
import io.gatling.http.Predef._

class CoronavirusLocalRestrictions extends Simulation {
  val postcodes = csv(dataDir + java.io.File.separatorChar + "all_postcodes.csv.gz").unzip.eager

  val duration = sys.props.getOrElse("duration", "3600").toInt
  var varyingPostcode = sys.props.getOrElse("varyingPostcode", "true").toBoolean

  def localRestrictionsPath(postcode: String, cachebust: String) = {
    val postcodeToUse = if (varyingPostcode) postcode else "SW1A 2AA"
    "/find-coronavirus-local-restrictions?postcode=" + postcodeToUse + "&cachebust=" + cachebust
  }

  val scn = scenario("Load test")
      .during(duration, "Soak test") {
        feed(cachebuster)
          .feed(postcodes.random)
          .exec(
            http("Check postcode")
              .get(localRestrictionsPath("${postcode}", "${cachebust}"))
          )
      }

  run(scn)
}
