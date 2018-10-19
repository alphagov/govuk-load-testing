package govuk

import io.gatling.core.Predef._
import io.gatling.http.Predef._

class Spider extends Simulation {
  val steps = sys.props.getOrElse("steps", "10").toInt

  val scn =
    scenario("Spider")
      .feed(cachebuster)
      .exec(session => session.set("href", "/"))
      .repeat(steps, "step") {
        exec(
          get("${href}",
              "${cachebust}-${step}",
              Seq(css("""a[href^="/"]""", "href").findRandom.saveAs("href"))))
      }

  run(scn)
}
