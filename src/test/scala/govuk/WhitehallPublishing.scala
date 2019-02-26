package govuk

import govuk.util.LoremIpsum
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.util.Random

/**
 * Scenario for Whitehall guidance.
 *
 * 1. Authenticates with signon
 * 2. Creates a draft publication
 * 3. Adds an HTML attachment
 * 4. Tags to taxonomy
 * 5. Force publish or force schedule
 */
class WhitehallPublishing extends Simulation {
  val WhitehallSchedulingMin = 15
  val lipsum = new LoremIpsum()
  val now = LocalDateTime.now()
  val format = DateTimeFormatter.ISO_LOCAL_DATE_TIME // "yyyy-MM-ddTHH:mm" // eg. 2019-01-11T17:00
  val maxTryAttempt = sys.props.getOrElse("maxTryAttempt", "1").toInt
  val schedule = sys.props.getOrElse("schedule", now.format(format))
  //val format = new java.text.SimpleDateFormat(formatStr)
  val scheduledAt = LocalDateTime.from(format.parse(schedule))
  // Only schedule if scheduledAt is more than 15 mins from now.
  val scheduled = scheduledAt.isAfter(now.plusMinutes(WhitehallSchedulingMin))

  if (scheduled) {
    println(s"Publishing scheduled for $scheduledAt")
  }

  println(s"Steps will be attemped $maxTryAttempt times")

  val scn =
    scenario("Publishing Whitehall guidance")
      .tryMax(maxTryAttempt) {
        exec(Signon.authenticate)
      }.exitHereIfFailed //If signon has failed all subsequents steps will also fail
      .tryMax(maxTryAttempt) {
        exec(
          http("Draft a new publication")
            .get("/government/admin/publications/new")
            .check(status.is(200))
            .check(regex("Create publication").exists)
            .check(
              css("input[name=authenticity_token]", "value").saveAs("authToken")
          )
        )
      }
      .exec(session => {
        val randomInt = Random.nextInt(Integer.MAX_VALUE)
        val publicationTitle = s"Gatling test publication $randomInt"
        var baseParams = Seq[(String, Any)](
          ("authenticity_token", session("authToken").as[String]),
          ("edition[publication_type_id]", "3"),
          ("edition[title]", publicationTitle),
          ("edition[summary]", s"$publicationTitle summary text"),
          ("edition[body]", s"""## Gatling test content\n\n${lipsum.text}"""),
          ("edition[lead_organisation_ids][]", "1056"),
          ("edition[previously_published]", "false")
        )
      if (scheduled) {
          baseParams = baseParams ++ Seq[(String, Any)](
            ("scheduled_publication_active", "1"),
            ("edition[scheduled_publication(1i)]", scheduledAt.getYear()),
            ("edition[scheduled_publication(2i)]", scheduledAt.getMonthValue()),
            ("edition[scheduled_publication(3i)]", scheduledAt.getDayOfMonth()),
            ("edition[scheduled_publication(4i)]", scheduledAt.getHour()),
            ("edition[scheduled_publication(5i)]", scheduledAt.getMinute())
          )
        }
        session.setAll(
          "randomInt"        -> randomInt,
          "publicationTitle" -> publicationTitle,
          "baseParams"       -> baseParams
        )
      })
      .tryMax(maxTryAttempt) {
        exec(
          http("Save a draft publication")
            .post("/government/admin/publications")
            .formParamSeq("${baseParams}")
            .check(status.is(200))
            .check(css(".form-actions span.or_cancel a", "href").saveAs("publicationLink"))
        )
      }
      .tryMax(maxTryAttempt) {
        exec(
          http("Draft overview")
            .get("""${publicationLink}""")
            .check(status.is(200))
            .check(
              css(".taxonomy-topics a.btn-default", "href").saveAs("addTagsLink"),
              css(".edition-view-edit-buttons a.btn-default", "href").saveAs("editDraftLink")
            )
            .check(checkIf(scheduled) {
              css(".edition-sidebar .button_to:nth-of-type(1)", "action").saveAs("forceScheduleAction")
            })
            .check(checkIf(scheduled) {
              css(".edition-sidebar .button_to:nth-of-type(1) input[name=authenticity_token]", "value").saveAs("forceScheduleAuthToken")
            })
            .check(checkIf(!scheduled) {
              css(".force-publish-form", "action").saveAs("forcePublishAction")
            })
            .check(checkIf(!scheduled) {
              css(".force-publish-form input[name=authenticity_token]", "value").saveAs("forcePublishAuthToken")
            })
        )
      }
      .tryMax(maxTryAttempt) {
        exec(
          http("Edit draft")
            .get("""${editDraftLink}""")
            .check(status.is(200))
            .check(regex("Edit publication").exists)
            .check(
              css(".nav-tabs li:nth-of-type(2) a", "href").saveAs("attachmentsLink")
            )
        )
      }
      .tryMax(maxTryAttempt) {
        exec(
          http("Visit HTML attachment form")
            .get("""${attachmentsLink}/new?type=html""")
            .check(status.is(200))
            .check(
              css("#new_attachment", "action").saveAs("attachmentFormAction"),
              css("#new_attachment input[name=authenticity_token]", "value").saveAs("attachmentAuthToken")
            )
        )
      }
      .tryMax(maxTryAttempt) {
        exec(
          http("Add HTML attachment")
            .post("""${attachmentFormAction}""")
            .formParam("authenticity_token", """${attachmentAuthToken}""")
            .formParam("type", "html")
            .formParam("attachment[title]", """${publicationTitle} attachment""")
            .formParam(
              "attachment[govspeak_content_attributes][body]",
              s"""## Gatling test attachment\n\n${lipsum.text}"""
            )
            .check(status.is(200))
        )
      }
      .tryMax(maxTryAttempt) {
        exec(Taxonomy.tag)
      }
      .doIfOrElse(scheduled) {
        tryMax(maxTryAttempt) {
          exec(
            http("Force schedule publication")
            .post("""${forceScheduleAction}""")
            .formParam("authenticity_token", """${forceScheduleAuthToken}""")
            .formParam("reason", "Gatling load test run")
            .formParam("commit", "Force schedule")
            .check(status.is(200))
          )
          .exec(
            http("Visit publication overview")
            .get("""${publicationLink}""")
            .check(status.is(200))
            .check(regex("Scheduled for publication boyo").exists)
          )
        }
      }{
        tryMax(maxTryAttempt) {
          exec(
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
        }
      }

  run(scn)
}
