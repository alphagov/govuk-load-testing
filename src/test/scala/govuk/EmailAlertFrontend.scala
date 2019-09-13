package govuk

import io.gatling.core.Predef._
import io.gatling.http.Predef._

/**
 * Email Alert Frontend scenario singleton object.
 *
 * Call 'exec(EmailAlertFrontend.subscribe)' to use in a Gatling scenario.
 * Requires 'emailAlertFrontendTopicId', 'emailAlertFrontendUrl', 'emailAlertFrontendAuthToken'
 * to be present in the user session.
 */
object EmailAlertFrontend {
  val subscribe = exec(
    http("Submit frequency")
      .post("""${emailAlertFrontendUrl}""")
      .formParam("authenticity_token", """${emailAlertFrontendAuthToken}""")
      .formParam("topic_id", """${emailAlertFrontendTopicId}""")
      .formParam("frequency", "immediately")
      .check(
        css(".checklist-email-signup", "action").saveAs("emailAlertFrontendEmailUrl"),
        css(".checklist-email-signup input[name=topic_id]", "value").saveAs("emailAlertFrontendTopicId"),
        css(".checklist-email-signup input[name=authenticity_token]", "value").saveAs("emailAlertFrontendAuthToken")
      )
      .check(status.is(200))
    )
    .exec(
      http("Submit email address")
        .post("""${emailAlertFrontendEmailUrl}""")
        .formParam("authenticity_token", """${emailAlertFrontendAuthToken}""")
        .formParam("topic_id", """${emailAlertFrontendTopicId}""")
        .formParam("frequency", "immediately")
        .formParam("address", "alice@example.com")
        .check(
          css(".govuk-panel__title")
        )
        .check(status.is(200))
    )
}
