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
        http("Draft a new publication")
          .get("/government/admin/publications/new")
          .check(status.is(200))
          .check(regex("Create publication").exists)
          .check(
            css("input[name=authenticity_token]", "value").saveAs("authToken")
          )
      )
      .exec(session => {
        val randomInt = Random.nextInt(Integer.MAX_VALUE)
        session.set("randomInt", randomInt)
        session.set("publicationTitle", s"Gatling test publication $randomInt")
      })
      .exec(
        http("Save a draft publication")
          .post("/government/admin/publications")
          .formParam("authenticity_token", """${authToken}""")
          .formParam("edition[publication_type_id]", "3")
          .formParam("edition[title]", """${publicationTitle}""")
          .formParam("edition[summary]", """${publicationTitle} summary text""")
          .formParam("edition[body]", """## This is a test publication

            TODO: Something to generate and/or include realistic content body.
            This isn't enough text to emulate the sort of payload Whitehall would
            typically send for a document"""
          )
          .formParam("edition[previously_published]", "false")
          .formParam("edition[lead_organisation_ids][]", "1056")
      )
      .exec(
        http("Visit documents index")
          .get("/government/admin/editions?organisation=1056&state=active")
          .check(status.is(200))
          .check(regex("My department’s documents").exists)
          .check(
            css("""a[title='View document ${publicationTitle}']""", "href").saveAs("publicationLink")
          )
      )
      .exec(
        http("Draft overview")
          .get("""${publicationLink}""")
          .check(status.is(200))
          .check(
            css(".taxonomy-topics a.btn-default", "href").saveAs("addTagsLink"),
            css(".edition-view-edit-buttons a.btn-default", "href").saveAs("editDraftLink"),
            css(".force-publish-form", "action").saveAs("forcePublishAction"),
            css(".force-publish-form input[name=authenticity_token]", "value").saveAs("forcePublishAuthToken")
          )
      )
      .exec(
        http("Edit draft")
          .get("""${editDraftLink}""")
          .check(status.is(200))
          .check(regex("Edit publication").exists)
          .check(
            css(".nav-tabs li:nth-of-type(2) a", "href").saveAs("attachmentsLink")
          )
      )
      .exec(
        http("Visit HTML attachment form")
          .get("""${attachmentsLink}/new?type=html""")
          .check(status.is(200))
          .check(
            css("#new_attachment", "action").saveAs("attachmentFormAction"),
            css("#new_attachment input[name=authenticity_token]", "value").saveAs("attachmentAuthToken")
          )
      )
      .exec(
        http("Add HTML attachment")
          .post("""${attachmentFormAction}""")
          .formParam("authenticity_token", """${attachmentAuthToken}""")
          .formParam("type", "html")
          .formParam("attachment[title]", """${publicationTitle} attachment""")
          .formParam("attachment[govspeak_content_attributes][body]", """
            ### ${publicationTitle} Attachment

            Some html attachment text.
            Maybe some govspeak:

            - This
            - That
            - Something else
          """
          )
          .check(status.is(200))
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
      .exec(
        http("Update draft tags")
          .put("""${saveTagsAction}""")
          .formParam("authenticity_token", """${authToken}""")
          .formParam("taxonomy_tag_form[previous_version]", "1")
          .formParam("taxonomy_tag_form[taxons][]", "e48ab80a-de80-4e83-bf59-26316856a5f9") // Could select these.
          .formParam("taxonomy_tag_form[taxons][]", "67f50352-bc30-482f-a2d0-a05714e3cea8")
          .check(status.is(200))
      )
      .exec(
        http("Force publish publication")
          .post("""${forcePublishAction}""")
          .formParam("authenticity_token", """${forcePublishAuthToken}""")
          .formParam("reason", "Gatling load test run")
          .formParam("commit", "Force publish")
          .check(status.is(200))
      )
      .exec(
        http("Visit publication overview")
          .get("""${publicationLink}""")
          .check(status.is(200))
          .check(regex("Force published: Gatling load test run").exists)
      )

    // This is a proof of concept so limit to 1 request
    // this can be scaled up with the usual call of `run(scn)`
    setUp(scn.inject(atOnceUsers(1))).protocols(httpProtocol)
}
