package no.nav.cv.event.oppfolgingstatus

import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Consumes
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Header
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.scheduling.annotation.Scheduled
import net.javacrumbs.shedlock.micronaut.SchedulerLock
import org.json.JSONObject
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Singleton

private val defaultPageSize = 10000L

@Singleton
open class OppfolgingsstatusRest (
        private val tokenProvider: TokenProvider,
        private val feedMetadataRepository: FeedMetadataRepository,
        private val oppfolgingsService: OppfolgingstatusService,
        private val oppfolgingsStatusFeedClient: OppfolgingsStatusFeedClient
) {
    companion object {
        val log = LoggerFactory.getLogger(OppfolgingsstatusRest::class.java)
    }

    @Scheduled(fixedDelay = "15s")
    @SchedulerLock(name = "oppfolgingfeed")
    open fun hentOppfolgingstatus() {
        log.debug("Siste feed id: ${feedMetadataRepository.sisteFeedId()}")
        try {
            val feed = oppfolgingsStatusFeedClient.feed(
                    authorization = "Bearer ${tokenProvider.get()}",
                    id = feedMetadataRepository.sisteFeedId(),
                    pageSize = defaultPageSize
            )

            val elements = feed.extractElements().sortedWith(feedComparator)
            log.debug("elements recieved ${elements.size}")

            elements.forEach {
                log.debug(it)
                oppfolgingsService.oppdaterStatus(it.toDto())
                feedMetadataRepository.oppdaterFeedId(it.feedId())
            }
        } catch (e: HttpClientResponseException) {
            if(e.status == HttpStatus.UNAUTHORIZED) tokenProvider.refresh() else throw e
        }

    }

}

private fun String.extractElements(): List<String> {
    val jsonObject = JSONObject(this)
    if(jsonObject.isNull("elements")) return listOf()
    return jsonObject.getJSONArray("elements").map { it.toString() }.toList()
}

private fun String.feedId(): Long {
    return JSONObject(this).getJSONObject("element").getLong("feedId")
}

private fun String.toDto(): OppfolgingstatusDto {
    return OppfolgingstatusDto(JSONObject(this).getJSONObject("element").toString())
}

private val feedComparator = Comparator { el1: String, el2: String -> el1.feedId().toInt() - el2.feedId().toInt() }



@Client("\${oppfolgingstatus.feed.host}")
interface OppfolgingsStatusFeedClient {

    @Consumes(MediaType.APPLICATION_JSON)
    @Get("/veilarboppfolging/api/feed/oppfolging")
    fun feed(
            @Header authorization: String,
            @QueryValue("id") id: Long,
            @QueryValue("page_size") pageSize: Long): String

}