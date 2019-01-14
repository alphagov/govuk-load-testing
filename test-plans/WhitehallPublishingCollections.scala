package govuk

import govuk.util.LoremIpsum
import io.gatling.commons.validation._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.util.Random

/**
 * Scenario for Whitehall collections.
 * NOTE: This scenario assumes that publications have already been published via
 * the 'WhitehallPublishing' scenario. Documents from this scenario will be added
 * to the collection.
 *
 * 1. Authenticates with signon
 * 2. Creates draft collection
 * 3. Adds documents to collection
 * 4. Tags collection to taxonomy
 * 5. Force publishes
 */
class WhitehallPublishingCollections extends Simulation {
  val lipsum = new LoremIpsum()
  val documentSearches = sys.props.getOrElse("documentSearches", "1").toInt

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
        session.setAll(
          "randomInt" -> randomInt,
          "collectionTitle" -> s"Gatling test collection $randomInt"
        )
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
          .check(status.is(200))
          .check(css(".form-actions span.or_cancel a", "href").saveAs("collectionLink"))
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
      // Whitehall document searches are hard-limited to 10 results per search so use a search suffix
      // to match on the random Int used when generating the publication title.
      .repeat(documentSearches, "index"){
        exec(session => {
          val suffix = session("index").as[Int] + 1
          session.set("suffix", suffix)
        })
        .exec(
          http("Search for documents")
            .get("""/government/admin/document_searches.json?state=published&title=gatling+test+publication+${suffix}""")
            .check(status.is(200))
            .check(jsonPath("$.results_any?").is("true"))
            .check(jsonPath("$.results[*].document_id").findAll.saveAs("documentIds"))
        )
        .pause(2) // Document search is slow and often 504s so give it some breathing space.
        .doIf("${documentIds.exists()}") {
          exec(session => {
            var documentIds = session("documentIds").as[Seq[Int]]
            if (session.contains("publicationDocumentIds")) {
              val existingDocumentIds = session("publicationDocumentIds").as[Seq[Int]]
              documentIds = existingDocumentIds ++ documentIds
            }
            session.set("publicationDocumentIds", documentIds)
          })
        }
      }
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
      .foreach(session => session("publicationDocumentIds").as[Seq[Int]], "documentId", "index"){
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

  run(scn)
}
