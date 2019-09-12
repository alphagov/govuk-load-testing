package govuk

import io.gatling.core.Predef._
import io.gatling.http.Predef._

class DynamicListsEmailSignup extends Simulation {
  val factor = sys.props.getOrElse("factor", "1").toFloat

  val duration = sys.props.getOrElse("duration", "0").toInt

  val paths = csv(dataDir + java.io.File.separatorChar + "get-ready-brexit-check-email-signup_paths.csv").readRecords

  val scale = factor / workers

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
                  css("#checklist-email-signup input[name=authenticity_token]", "value").saveAs("subscribeAuthToken")
                )
                .check(status.is(200))
            )
              .exec(
                http("POST Subscribe")
                  .post("""${subscribeFormAction}""")
                  .formParam("authenticity_token", """${subscribeAuthToken}""")
                  .check(
                    css(".checklist-email-signup", "action").saveAs("frequencyLink"),
                    css(".checklist-email-signup input[name=topic_id]", "value").saveAs("emailSubscriptionFrequencyTopicId"),
                    css(".checklist-email-signup input[name=authenticity_token]", "value").saveAs("subscribeAuthToken")
                  )
                  .check(status.is(200))
              )
              .exec(
                http("POST Frequency")
                  .post("""${frequencyLink}""")
                  .formParam("authenticity_token", """${subscribeAuthToken}""")
                  .formParam("topic_id", """${emailSubscriptionFrequencyTopicId}""")
                  .formParam("frequency", "immediately")
                  .check(
                    css(".checklist-email-signup", "action").saveAs("emailSubscriptionFrequency"),
                    css(".checklist-email-signup input[name=topic_id]", "value").saveAs("emailSubscriptionFrequencyTopicId"),
                    css(".checklist-email-signup input[name=authenticity_token]", "value").saveAs("subscribeAuthToken")
                  )
                  .check(status.is(200))
              )
              .exec(
                http("POST Email address")
                  .post("""${emailSubscriptionFrequency}""")
                  .formParam("authenticity_token", """${subscribeAuthToken}""")
                  .formParam("topic_id", """${emailSubscriptionFrequencyTopicId}""")
                  .formParam("frequency", "immediately")
                  .formParam("address", "alice@example.com")
                  .check(
                    css(".govuk-panel__title")
                  )
                  .check(status.is(200))
              )
          }
      }

  val scn_with_duration =
    scenario("DynamicListsEmailSignup")
      .during(duration, "Soak test"){
        feed(cachebuster)
        .foreach(paths, "path") {
          exec(flattenMapIntoAttributes("${path}"))
            .repeat(session => math.ceil(session("hits").as[Int] * scale).toInt, "hit") {
              exec(
                get("${base_path}", "${cachebust}-${hit}")
                  .check(
                    css("#checklist-email-signup", "action").saveAs("subscribeFormAction"),
                    css("#checklist-email-signup input[name=authenticity_token]", "value").saveAs("subscribeAuthToken")
                  )
                  .check(status.is(200))
              )
              .exec(
                http("POST Subscribe")
                  .post("""${subscribeFormAction}""")
                  .formParam("authenticity_token", """${subscribeAuthToken}""")
                  .check(
                    css(".checklist-email-signup", "action").saveAs("frequencyLink"),
                    css(".checklist-email-signup input[name=topic_id]", "value").saveAs("emailSubscriptionFrequencyTopicId"),
                    css(".checklist-email-signup input[name=authenticity_token]", "value").saveAs("subscribeAuthToken")
                  )
                  .check(status.is(200))
              )
              .exec(
                http("POST Frequency")
                  .post("""${frequencyLink}""")
                  .formParam("authenticity_token", """${subscribeAuthToken}""")
                  .formParam("topic_id", """${emailSubscriptionFrequencyTopicId}""")
                  .formParam("frequency", "immediately")
                  .check(
                    css(".checklist-email-signup", "action").saveAs("emailSubscriptionFrequency"),
                    css(".checklist-email-signup input[name=topic_id]", "value").saveAs("emailSubscriptionFrequencyTopicId"),
                    css(".checklist-email-signup input[name=authenticity_token]", "value").saveAs("subscribeAuthToken")
                  )
                  .check(status.is(200))
              )
              .exec(
                http("POST Email address")
                  .post("""${emailSubscriptionFrequency}""")
                  .formParam("authenticity_token", """${subscribeAuthToken}""")
                  .formParam("topic_id", """${emailSubscriptionFrequencyTopicId}""")
                  .formParam("frequency", "immediately")
                  .formParam("address", "alice@example.com")
                  .check(
                    css(".govuk-panel__title")
                  )
                  .check(status.is(200))
              )
            }
        }
      }

  if(duration > 0){
    run(scn_with_duration)
  } else{
    run(scn)
  }
}
