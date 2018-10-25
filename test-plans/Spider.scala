package govuk

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.util.Random

class Spider extends Simulation {
  val minSteps = sys.props.getOrElse("minSteps", "5").toInt
  val maxSteps = sys.props.getOrElse("maxSteps", "50").toInt
  val steps = sys.props.get("steps")
  val startPage = sys.props.getOrElse("startPage", "/")

  val stepper = Iterator.continually(Map("steps" -> (steps match {
    // giving a "steps" value overrides the min/max steps
    case Some(value) => value.toInt
    // +1 because the upper bound of 'nextInt' is exclusive
    case None => Random.nextInt(1 + maxSteps - minSteps) + minSteps
  })))

  val scn =
    scenario("Spider")
      .feed(cachebuster)
      .feed(stepper)
      .exec(session => session.set("href", startPage))
      .repeat("${steps}", "step") {
        exec(
          get("${href}",
              "${cachebust}-${step}",
              Seq(css("""a[href^="/"]""", "href").findRandom.saveAs("href"))))
      }

  run(scn)
}
