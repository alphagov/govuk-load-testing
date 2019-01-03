package govuk

import io.gatling.core.Predef._
import io.gatling.http.Predef._

/**
 * Signon scenario singleton object.
 *
 * Env vars USERNAME and PASSWORD are used to authenticate with the signon
 * application configured using the JAVA_OPT 'signonUrl'.
 * Call 'exec(Signon.authenticate)' to use in a Gatling scenario.
 */
object Signon {
  val signonBaseUrl = sys.props.get("signonUrl").get
  val signonUrl = s"$signonBaseUrl/users/sign_in"

  val authenticate = exec(
    http("Visit signon")
      .get(signonUrl)
      .check(status.is(200))
      .check(
        regex("Sign in to GOV.UK").exists,
        css("input[name=authenticity_token]", "value").saveAs("signonAuthToken")
      )
  )
  .exec(
    http("Authenticate in signon")
      .post(signonUrl)
      .formParam("authenticity_token", """${signonAuthToken}""")
      .formParam("user[email]", sys.env.get("USERNAME").get)
      .formParam("user[password]", sys.env.get("PASSWORD").get)
      .check(status.is(200))
  )
  .exec(
    http("View signon account")
      .get(signonBaseUrl)
      .check(status.is(200))
      .check(regex("Your applications").exists)
  )
}
