package govuk

import io.gatling.core.Predef._
import io.gatling.http.Predef._

/**
 * Taxonomy scenario singleton object.
 *
 * Call 'exec(Taxonomy.tag)' to use in a Gatling scenario.
 * Requires 'addTagsLink' to be present in the user session,
 * this should be the url to the taxonomy tagging page for the
 * relevant Whitehall edition.
 */
object Taxonomy {
  val tag = exec(
    http("Edit tags")
      .get("""${addTagsLink}""")
      .check(status.is(200))
      .check(regex("Topics").exists)
      .check(
        css(".new_taxonomy_tag_form", "action").saveAs("saveTagsAction"),
        css(".new_taxonomy_tag_form input[name=authenticity_token]", "value").saveAs("authToken")
      )
  )
  .exec(
    http("Update tags")
      .put("""${saveTagsAction}""")
      .formParam("authenticity_token", """${authToken}""")
      .formParam("taxonomy_tag_form[previous_version]", "1")
      .formParam("taxonomy_tag_form[taxons][]", "e48ab80a-de80-4e83-bf59-26316856a5f9")
      .formParam("taxonomy_tag_form[taxons][]", "67f50352-bc30-482f-a2d0-a05714e3cea8")
      .check(status.is(200))
      .check(regex("The tags have been updated.").exists)
  )
}
