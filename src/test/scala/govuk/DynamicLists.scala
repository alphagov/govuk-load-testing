package govuk

import io.gatling.core.Predef._
import io.gatling.http.Predef._

class DynamicLists extends Simulation {
  val factor = sys.props.getOrElse("factor", "1").toFloat

  val paths = csv(dataDir + java.io.File.separatorChar + "get-ready-brexit-check_paths.csv").readRecords

  val scale = factor / workers

  val scn =
    scenario("DynamicLists")
      .feed(cachebuster)
      .foreach(paths, "path") {
        exec(flattenMapIntoAttributes("${path}"))
          .repeat(session => math.ceil(session("hits").as[Int] * scale).toInt,
                  "hit") {
            exec(get("${base_path}", "${cachebust}-${hit}"))
          }
      }

  run(scn)
}
