package govuk

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.util.Random

class WhitehallPublishing extends Simulation {
  val factor = sys.props.getOrElse("factor", "1").toFloat
  val scale = factor / workers
  val signonUrl = sys.props.get("signonUrl").get + "/users/sign_in"

  val scn =
    scenario("Publishing Whitehall guidance")
      .exec(
        http("Visit signon")
          .get(signonUrl)
          .check(status.is(200))
          .check(
            regex("Sign in to GOV.UK").exists,
            css("input[name=authenticity_token]", "value").saveAs("authToken")
          )
      )
      .exec(
        http("Authenticate in signon")
          .post(signonUrl)
          .formParam("authenticity_token", """${authToken}""")
          .formParam("user[email]", sys.env.get("USERNAME").get)
          .formParam("user[password]", sys.env.get("PASSWORD").get)
          .check(status.is(200))
      )
      .exec(
        http("Visit Whitehall admin backend")
          .get("/government/admin")
          .check(status.is(200))
          .check(regex("GOV.UK Whitehall").exists)
          .check(regex("New document").exists)
      )
      .exec(
        http("Visit new case study form")
          .get("/government/admin/case-studies/new")
          .check(status.is(200))
          .check(regex("Create case study").exists)
          .check(
            css("input[name=authenticity_token]", "value").saveAs("authToken")
          )
      )
      .exec(session => {
        session.set("randomInt", Random.nextInt(Integer.MAX_VALUE))
      })
      .exec(
        http("Save a draft case study")
          .post("/government/admin/case-studies")
          .formParam("authenticity_token", """${authToken}""")
          .formParam("edition[title]", """Gatling test case study ${randomInt}""")
          .formParam("edition[summary]", "Gatling test case study")
          .formParam("edition[body]", """## Gatling test case study

            TODO: Method to generate realistic case study body.
            This isn't enough text to emulate the sort of payload Whitehall would
            typically send for a case study"""
          )
          .formParam("edition[previously_published]", "false")
          .formParam("edition[lead_organisation_ids][]", "1056")
      )
      .pause(2)
      .exec(
        http("Visit documents index")
          .get("/government/admin/editions?organisation=1056&state=active")
          .check(status.is(200))
          .check(regex("My department’s documents").exists)
          .check(
            css("""a[title='View document Gatling test case study ${randomInt}']""", "href").saveAs("caseStudyLink")
          )
      )
      .pause(2)
      .exec(
        http("Edit draft")
          .get("""${caseStudyLink}""")
          .check(status.is(200))
          .check(
            css(".taxonomy-topics a.btn-default", "href").saveAs("addTagsLink"),
            css(".force-publish-form", "action").saveAs("forcePublishAction"),
            css(".force-publish-form input[name=authenticity_token]", "value").saveAs("forcePublishAuthToken")
          )
      )
      .exec(
        http("Edit draft tags")
          .get("""${addTagsLink}""")
          .check(status.is(200))
          .check(
            css(".new_taxonomy_tag_form", "action").saveAs("saveTagsAction"),
            css(".new_taxonomy_tag_form input[name=authenticity_token]", "value").saveAs("authToken")
          )
      )
      /*
       * Example debugging EL vars as they are saved to the Gatling session
      .exec(session => {
        val addTagsLink = session("addTagsLink").as[String]
        println(s"addTagsLink: $addTagsLink")
        val saveTagsAction = session("saveTagsAction").as[String]
        println(s"saveTagsAction: $saveTagsAction")
        val forcePublishAction = session("forcePublishAction").as[String]
        println(s"forcePublishAction: $forcePublishAction")
        session
      })
      */
      .exec(
        http("Update draft tags")
          .put("""${saveTagsAction}""")
          .formParam("authenticity_token", """${authToken}""")
          .formParam("taxonomy_tag_form[previous_version]", "1")
          .formParam("taxonomy_tag_form[taxons][]", "e48ab80a-de80-4e83-bf59-26316856a5f9") // Could select these.
          .formParam("taxonomy_tag_form[taxons][]", "67f50352-bc30-482f-a2d0-a05714e3cea8")
          .check(status.is(200))
      )
      .pause(2)
      .exec(
        http("Force publish case study")
          .post("""${forcePublishAction}""")
          .formParam("authenticity_token", """${forcePublishAuthToken}""")
          .formParam("reason", "Gatling load test run")
          .formParam("commit", "Force publish")
          .check(status.is(200))
      )

    // This is a proof of concept so limit to 1 request
    // this can be scaled up with the usual call of `run(scn)`
    setUp(scn.inject(atOnceUsers(1))).protocols(httpProtocol)
}
