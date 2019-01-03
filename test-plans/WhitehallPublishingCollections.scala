package govuk

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.util.Random

class WhitehallPublishingCollections extends Simulation {
  val factor = sys.props.getOrElse("factor", "1").toFloat
  val scale = factor / workers

  val scn =
    scenario("Publishing Whitehall collections")
      .exec(Signon.authenticate)
      .exec(
        http("Draft a new collection")
          .get("/government/admin/collections/new")
          .check(status.is(200))
          .check(regex("Create document collection").exists)
          .check(
            css("input[name=authenticity_token]", "value").saveAs("authToken")
          )
      )
      .exec(session => {
        val randomInt = Random.nextInt(Integer.MAX_VALUE)
        session.set("randomInt", randomInt)
        session.set("collectionTitle", s"Gatling test collection $randomInt")
      })
      .exec(
        http("Save a draft collection")
          .post("/government/admin/collections")
          .formParam("authenticity_token", """${authToken}""")
          .formParam("edition[lock_version]", "0")
          .formParam("edition[title]", """${collectionTitle}""")
          .formParam("edition[summary]", """${collectionTitle} summary text""")
          .formParam("edition[body]", """## Gatling test collection

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
            css("""a[title='View document ${collectionTitle}']""", "href").saveAs("collectionLink")
          )
      )
      .exec(
        http("Draft collection overview")
          .get("""${collectionLink}""")
          .check(status.is(200))
          .check(
            css(".taxonomy-topics a.btn-default", "href").saveAs("addTagsLink"),
            css(".edition-view-edit-buttons a.btn-default", "href").saveAs("editDraftLink"),
            css(".force-publish-form", "action").saveAs("forcePublishAction"),
            css(".force-publish-form input[name=authenticity_token]", "value").saveAs("forcePublishAuthToken")
          )
      )
      .exec(
        http("Edit draft collection")
          .get("""${editDraftLink}""")
          .check(status.is(200))
          .check(regex("Edit publication").exists)
          .check(
            css(".nav-tabs li:nth-of-type(2) a", "href").saveAs("attachmentsLink")
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
