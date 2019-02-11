package govuk

import io.gatling.core.Predef._
import io.gatling.http.Predef._

class PostHomepage extends Simulation {
  val post = repeat(80000, "n") {
    exec(http("POST /").post("/").check(status.is(404)))
  }

  val scn =
    scenario("PostHomepage")
      .exec(post)

  run(scn)
}
