package no.nav.cv.event.oppfolgingstatus

import io.micronaut.http.client.HttpClient
import io.micronaut.scheduling.annotation.Scheduled
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OppfolgingsstatusRest (
        private val oppfolgingsService: OppfolgingstatusService
) {
    companion object {
        val log = LoggerFactory.getLogger(OppfolgingsstatusRest::class.java)
    }

    @Inject
    private lateinit var client: HttpClient

    private var oidcToken: String? = null

    init {
        refreshToken()
    }

    @Scheduled(fixedDelay = "15s")
    fun hentOppfolgingstatus() {

        val currentOidcToken = oidcToken

        if(currentOidcToken == null) {
            log.error("Mangler OIDC token når vi skal hente oppfølgingsstatus")
            return
        }

        // hent nye saker fra rest og spol igjennom med oppfolgingsService.oppdaterStatus()
    }

    @Scheduled(fixedDelay = "3500s")
    fun refreshToken() {
        oidcToken = null


        // token client her
    }
}

