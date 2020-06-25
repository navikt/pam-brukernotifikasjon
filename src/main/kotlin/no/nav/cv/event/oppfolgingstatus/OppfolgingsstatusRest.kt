package no.nav.cv.event.oppfolgingstatus

import io.micronaut.context.annotation.Value
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.scheduling.annotation.Scheduled
import org.json.JSONObject
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton


private val uninitialized = "uninitialized"
@Singleton
class OppfolgingsstatusRest (
        private val oppfolgingsService: OppfolgingstatusService,
        private val oppfolgingsStatusFeedClient: OppfolgingsStatusFeedClient,
        private val securityTokeServiceClient: SecurityTokeServiceClient,
        @Value("\${oppfolgingstatus.sts.serviceuser.username}") val user: String,
        @Value("\${oppfolgingstatus.sts.serviceuser.password}") val pw: String
) {
    companion object {
        val log = LoggerFactory.getLogger(OppfolgingsstatusRest::class.java)
    }

    // /veilarboppfolging/api/feed/oppfolging
    // /rest/v1/sts/token

//    @Inject
//    private lateinit var client: HttpClient

    private var oidcToken: String = uninitialized

    @Scheduled(fixedDelay = "15s")
    fun hentOppfolgingstatus() {

        if(oidcToken == uninitialized) refreshToken()

        try {
            val response = oppfolgingsStatusFeedClient.feed(
                    authorization = "Bearer $oidcToken",
                    id = 1,
                    pageSize = 20
            )
            log.debug(response.body()!!)
        } catch (e: HttpClientResponseException) {
            if(e.status == HttpStatus.UNAUTHORIZED) refreshToken() else throw e
        }

    }

    //@Scheduled(fixedDelay = "3500s")
    fun refreshToken() {
        log.debug("Refreshing token")
        val auth = Base64.getEncoder().encodeToString("$user:$pw".toByteArray(Charsets.UTF_8))
        val response = securityTokeServiceClient.authenticate("Basic $auth")

        JSONObject(response).query("/access_token")
        oidcToken = JSONObject(response).getString("access_token")
                ?: uninitialized
    }
}

