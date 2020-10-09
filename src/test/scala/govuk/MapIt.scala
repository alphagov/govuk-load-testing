package govuk

import io.gatling.core.Predef._
import io.gatling.http.Predef._

class MapIt extends Simulation {
  val factor = sys.props.getOrElse("factor", "1").toFloat
  val duration = sys.props.getOrElse("duration", "0").toInt

  val postcodes = csv(dataDir + java.io.File.separatorChar + "postcodes.csv").readRecords

  val scale = factor / workers

  val scn =
    scenario("MapIt")
      .feed(cachebuster)
      .foreach(postcodes, "postcode") {
        exec(flattenMapIntoAttributes("${postcode}"))
          .repeat(session => math.ceil(session("hits").as[Int] * scale).toInt,
                  "hit") {
            exec(get("${base_path}", "${cachebust}-${hit}"))
          }
      }

  val scnWithDuration =
    scenario("Mapit")
      .during(duration, "Soak test") {
        feed(cachebuster)
        .foreach(postcodes, "postcode") {
          exec(flattenMapIntoAttributes("${postcode}"))
            .repeat(session => math.ceil(session("hits").as[Int] * scale).toInt,
                    "hit") {
              exec(get("${base_path}", "${cachebust}-${hit}"))
            }
        }
      }

  if (duration == 0) run(scn) else run(scnWithDuration)
}
