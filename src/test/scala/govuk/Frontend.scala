package govuk

import io.gatling.core.Predef._
import io.gatling.http.Predef._

class Frontend extends Simulation {
  val factor = sys.props.getOrElse("factor", "1").toFloat

  val duration = sys.props.getOrElse("duration", "0").toInt

  val paths = csv(dataDir + java.io.File.separatorChar + "paths.csv").readRecords

  val scale = factor / workers

  val scn =
    scenario("Frontend")
      .feed(cachebuster)
      .foreach(paths, "path") {
        exec(flattenMapIntoAttributes("${path}"))
          .repeat(session => math.ceil(session("hits").as[Int] * scale).toInt,
                  "hit") {
            exec(get("${base_path}", "${cachebust}-${hit}"))
          }
      }

  val scn_with_duration =
    scenario("Frontend")
      .during(duration, "Soak test"){
        feed(cachebuster)
        .foreach(paths, "path") {
          exec(flattenMapIntoAttributes("${path}"))
            .repeat(session => math.ceil(session("hits").as[Int] * scale).toInt,
                    "hit") {
              exec(get("${base_path}", "${cachebust}-${hit}"))
            }
        }
      }

  if(duration > 0){
    run(scn_with_duration)
  } else{
    run(scn)
  }
}
