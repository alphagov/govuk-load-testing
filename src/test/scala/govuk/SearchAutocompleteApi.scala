import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class SearchAutocompleteApi extends Simulation {
    val dataDir = sys.props.getOrElse("dataDir", "src/test/resources/test-data")
    val searches = csv(dataDir + java.io.File.separatorChar + "autocomplete-searches.csv").random
    val httpProtocol = http.baseUrl("https://search-autocomplete-api.staging.publishing.service.gov.uk")
    val duration = sys.props.getOrElse("duration", "120").toInt

     val scn = scenario("Load test")
         .during(duration, "Soak test") {
           feed(searches)
             .exec(
               http("Search: ${search_term}")
                 .get("${search_term}")
           )
     }
    val maxTime = sys.props.getOrElse("maxTime", "5000").toInt
    setUp(
      scn.inject(constantUsersPerSec(5) during (120 seconds)).protocols(httpProtocol)
    ).maxDuration(maxTime seconds)
}
