package govuk

import govuk.util.LoremIpsum
import io.gatling.commons.validation._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.util.Random

class WhitehallPublishingCollections extends Simulation {
  val factor = sys.props.getOrElse("factor", "1").toFloat
  val scale = factor / workers
  val lipsum = new LoremIpsum()

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
          .formParam("edition[body]", s"""## Gatling test collection\n\n${lipsum.text}""")
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
          .check(regex("Edit document collection").exists)
          .check(
            css(".nav-tabs li:nth-of-type(2) a", "href").saveAs("documentsLink")
          )
      )
      .exec(
        http("Search for documents")
          .get("/government/admin/document_searches.json?title=gatling+test+publication")
          .check(status.is(200))
          .check(jsonPath("$.results_any?").is("true"))
          .check(jsonPath("$.results[*].document_id").findAll.saveAs("documentIds"))
      )
      .exec(
        http("Visit collection documents form")
          .get("""${documentsLink}""")
          .check(status.is(200))
          .check(regex("Add document to a group").exists)
          .check(
            css("form.document-finder", "action").saveAs("addDocumentFormAction"),
            css("form.document-finder input[name=authenticity_token]", "value").saveAs("documentFinderAuthToken"),
            css("form.document-finder select[name=group_id] option:nth-of-type(1)", "value").saveAs("groupId")
          )
      )
      .foreach(session => session("documentIds").as[Seq[Int]], "documentId", "index"){
        exec(
          http("Add document to collection")
            .post("""${addDocumentFormAction}""")
            .formParam("authenticity_token", """${documentFinderAuthToken}""")
            .formParam("document_id", """${documentId}""")
            .formParam("group_id", """${groupId}""")
            .check(status.is(200))
        )
      }
      .exec(Taxonomy.tag)
      .exec(
        http("Force publish collection")
          .post("""${forcePublishAction}""")
          .formParam("authenticity_token", """${forcePublishAuthToken}""")
          .formParam("reason", "Gatling load test run")
          .formParam("commit", "Force publish")
          .check(status.is(200))
      )
      .exec(
        http("Visit collection overview")
          .get("""${collectionLink}""")
          .check(status.is(200))
          .check(regex("Force published: Gatling load test run").exists)
      )

    // This is a proof of concept so limit to 1 request
    // this can be scaled up with the usual call of `run(scn)`
    setUp(scn.inject(atOnceUsers(1))).protocols(httpProtocol)
}
