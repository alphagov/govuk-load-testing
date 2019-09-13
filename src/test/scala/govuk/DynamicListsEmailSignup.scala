package govuk

import io.gatling.core.Predef._
import io.gatling.http.Predef._

class DynamicListsEmailSignup extends Simulation {
  val factor = sys.props.getOrElse("factor", "1").toFloat
  val duration = sys.props.getOrElse("duration", "0").toInt

  val paths = csv(dataDir + java.io.File.separatorChar + "get-ready-brexit-check-email-signup_paths.csv").readRecords

  val scale = factor / workers

  val subscribe = exec(
    http("Subscribe")
      .post("""${subscribeFormAction}""")
      .formParam("authenticity_token", """${emailAlertFrontendAuthToken}""")
      .check(
        css(".checklist-email-signup", "action").saveAs("emailAlertFrontendUrl"),
        css(".checklist-email-signup input[name=topic_id]", "value").saveAs("emailAlertFrontendTopicId"),
        css(".checklist-email-signup input[name=authenticity_token]", "value").saveAs("emailAlertFrontendAuthToken")
      )
      .check(status.is(200))
  )

  val scn =
    scenario("DynamicListsEmailSignup")
      .feed(cachebuster)
      .foreach(paths, "path") {
        exec(flattenMapIntoAttributes("${path}"))
          .repeat(session => math.ceil(session("hits").as[Int] * scale).toInt, "hit") {
            exec(
              get("${base_path}", "${cachebust}-${hit}")
                .check(
                  css("#checklist-email-signup", "action").saveAs("subscribeFormAction"),
                  css("#checklist-email-signup input[name=authenticity_token]", "value").saveAs("emailAlertFrontendAuthToken")
                )
                .check(status.is(200))
            )
            .exec(subscribe)
            .exec(EmailAlertFrontend.subscribe)
          }
      }

  val scnWithDuration =
    scenario("DynamicListsEmailSignup")
      .during(duration, "Soak test") {
        feed(cachebuster)
        .foreach(paths, "path") {
          exec(flattenMapIntoAttributes("${path}"))
            .repeat(session => math.ceil(session("hits").as[Int] * scale).toInt, "hit") {
              exec(
                get("${base_path}", "${cachebust}-${hit}")
                  .check(
                    css("#checklist-email-signup", "action").saveAs("subscribeFormAction"),
                    css("#checklist-email-signup input[name=authenticity_token]", "value").saveAs("emailAlertFrontendAuthToken")
                  )
                  .check(status.is(200))
              )
              .exec(subscribe)
              .exec(EmailAlertFrontend.subscribe)
            }
        }
      }

  if (duration == 0) run(scn) else run(scnWithDuration)
}
