package govuk

import io.gatling.core.Predef._
import io.gatling.http.Predef._

class RegisterToVote extends Simulation {
  val duration = sys.props.getOrElse("duration", "0").toInt

  val voteUrl = s"$baseUrl/register-to-vote"

  val scn =
    scenario("Register To Vote")
      .feed(cachebuster)
      .exec(
        http(s"Visit Register To Vote - $voteUrl")
            .get(voteUrl)
            .check(status.is(200))
          )

  val scnWithDuration =
    scenario("Register To Vote")
      .during(duration, "Soak test") {
        feed(cachebuster)
        .exec(
          http(s"Visit Register To Vote - $voteUrl")
              .get(voteUrl)
              .check(status.is(200))
            )
      }

  if (duration == 0) run(scn) else run(scnWithDuration)
}
