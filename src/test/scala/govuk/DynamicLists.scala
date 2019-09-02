package govuk

import io.gatling.core.Predef._
import io.gatling.http.Predef._

class DynamicLists extends Simulation {
  val factor = sys.props.getOrElse("factor", "1").toFloat
  val duration = sys.props.getOrElse("duration", "0").toInt

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


  val scn2 =
    scenario("DynamicLists")
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
    run(scn2)
  } else{
    run(scn)
  }
}
