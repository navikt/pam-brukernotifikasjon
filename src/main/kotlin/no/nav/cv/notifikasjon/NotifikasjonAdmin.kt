package no.nav.cv.notifikasjon

import no.nav.cv.output.OutboxService
import no.nav.security.token.support.core.api.Unprotected
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.*


@RestController
@Unprotected
@RequestMapping("internal/kafka/manuell")
class NotifikasjonAdmin(
        @Value("\${admin.enabled}") private val adminEnabled: Boolean,
        private val outboxService: OutboxService,
        private val adminService: AdminService,
) {

    companion object {
        private val log = LoggerFactory.getLogger(NotifikasjonAdmin::class.java)
    }

    init {
        log.info("Startet med admin-modus satt til: ${adminEnabled}")
    }

    @GetMapping("varsel/{uuid}/{fnr}", produces = [ "text/plain" ])
    fun varsle(
            @PathVariable("uuid") uuid: UUID,
            @PathVariable("fnr") fnr: String
    ): String {
        log.warn("Call for NotifikasjonAdmin with adminEnabled: ${adminEnabled}")
        if(!adminEnabled) throw ResponseStatusException(HttpStatus.UNAUTHORIZED)

        log.info("NotifikasjonAdmin genererer varsel oppgave for uuid $uuid")

        outboxService.schdeuleVarsel(uuid, "admin", fnr)
        return "OK"
    }

    @GetMapping("donemelding/{uuid}/{fnr}", produces = [ "text/plain" ])
    fun done(
            @PathVariable("uuid") uuid: UUID,
            @PathVariable("fnr") fnr: String
    ): String {
        log.warn("Call for NotifikasjonAdmin with adminEnabled: ${adminEnabled}")

        if(!adminEnabled) throw ResponseStatusException(HttpStatus.UNAUTHORIZED)

        log.info("NotifikasjonAdmin genererer done melding for uuid $uuid")

        outboxService.schdeuleDone(uuid, "admin", fnr)
        return "OK"
    }


    @GetMapping("lukk-alle", produces = [ "text/plain" ])
    fun done(): String {
        log.warn("Call for NotifikasjonAdmin with adminEnabled: ${adminEnabled}")
        if(!adminEnabled) throw ResponseStatusException(HttpStatus.UNAUTHORIZED)

        log.info("NotifikasjonAdmin genererer done melding alle aktive")
        val varsler = adminService.hentÅpneVarsler()

        log.info("Fant ${varsler.size} åpne varlser")

        var i = 0
        varsler.forEach {
            adminService.lukkVarsel(it)
            if (i % 100 == 0) log.info("Lukket $i av / ${varsler.size} varsler")
            i++
        }

        log.info("Markerte ${varsler.size} statuser med varslet som ferdig og skedulerte DONE-melding")

        return "OK"
    }
}
