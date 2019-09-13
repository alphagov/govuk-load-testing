package govuk

import io.gatling.core.Predef._
import io.gatling.http.Predef._

/**
 * Email Alert Frontend scenario singleton object.
 *
 * Call 'exec(EmailAlertFrontend.subscribe)' to use in a Gatling scenario.
 * Requires 'emailSubscriptionTopicId', 'frequencyLink', 'subscribeAuthToken'
 * to be present in the user session.
 */
object EmailAlertFrontend {
  val subscribe = exec(
    http("Submit frequency")
      .post("""${frequencyLink}""")
      .formParam("authenticity_token", """${subscribeAuthToken}""")
      .formParam("topic_id", """${emailSubscriptionTopicId}""")
      .formParam("frequency", "immediately")
      .check(
        css(".checklist-email-signup", "action").saveAs("emailSubscriptionFrequency"),
        css(".checklist-email-signup input[name=topic_id]", "value").saveAs("emailSubscriptionTopicId"),
        css(".checklist-email-signup input[name=authenticity_token]", "value").saveAs("subscribeAuthToken")
      )
      .check(status.is(200))
    )
    .exec(
      http("Submit email address")
        .post("""${emailSubscriptionFrequency}""")
        .formParam("authenticity_token", """${subscribeAuthToken}""")
        .formParam("topic_id", """${emailSubscriptionTopicId}""")
        .formParam("frequency", "immediately")
        .formParam("address", "alice@example.com")
        .check(
          css(".govuk-panel__title")
        )
        .check(status.is(200))
    )
}
