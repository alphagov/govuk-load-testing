package govuk

import govuk.util.LoremIpsum
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import scala.util.Random

/**
 * Publish content to Publishing API
 *
 * 1. Put draft content
 * 2. Put links
 * 3. Publish
 */
class PublishToPublishingApi extends Simulation {

  val lipsum = new LoremIpsum()
  val bearerToken = sys.env.get("BEARER_TOKEN").get
  val bearerTokenHeaderValue = s"Bearer $bearerToken"

  val detailedGuideContentPath = dataDir + java.io.File.separatorChar + "publishing-api/detailed-guide-content.json"
  val publishPath = dataDir + java.io.File.separatorChar + "publishing-api/publish.json"

  val scn =
    scenario("Publishing to Publishing API")
      .exec(session => {
        val basePath = s"/guidance/${UUID.randomUUID()}"
        val contentId = UUID.randomUUID()
        val publicUpdatedAt = ZonedDateTime.now().format(
          DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"))

        session.setAll(
          "basePath"        -> basePath,
          "body"            -> lipsum.text,
          "contentId"       -> contentId,
          "description"     -> lipsum.sentence,
          "organisationId"  -> "af07d5a5-df63-4ddc-9383-6a666845ebe9", // GDS
          "publicUpdatedAt" -> publicUpdatedAt,
          "title"           -> lipsum.sentence
        )
      })
      .exec(
        http("PUT content")
          .put("""/v2/content/${contentId}""")
          .headers(Map(
            "Authorization" -> bearerTokenHeaderValue,
            "Content-type"  -> "application/json"
          ))
          .body(ElFileBody(detailedGuideContentPath)).asJson
          .check(bodyString.saveAs("responseBody"))
          .check(status.is(200))
      )
      .exec(session => {
        val response = session("responseBody").as[String]
        println(s"response:\n$response")
        session
      })
      // .exec(
      //   http("PATCH links")
      //     .patch("/v2/links")
      //     .body(RawFileBody("/path/to/some/links.json")).asJson
      //     .check(status.is(200))
      // )
      .exec(
        http("POST publish")
          .post("/v2/content/${contentId}/publish")
          .headers(Map(
            "Authorization" -> bearerTokenHeaderValue,
            "Content-type"  -> "application/json"
          ))
          .body(ElFileBody(publishPath)).asJson
          .check(status.is(200))
      )
  run(scn)
}
