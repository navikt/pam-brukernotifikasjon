package no.nav.cv.notifikasjon

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.*


@RestController
@RequestMapping("/internal/kafka/manuell")
class NotifikasjonAdmin(
        @Value("\${admin.enabled}") private val adminEnabled: String,
        private val varselPublisher: VarselPublisher
) {

    companion object {
        private val log = LoggerFactory.getLogger(NotifikasjonAdmin::class.java)
    }


    @GetMapping("varsel/{uuid}/{fnr}", produces = [ "text/plain" ])
    fun varsle(
            @PathVariable("uuid") uuid: UUID,
            @PathVariable("fnr") fnr: String
    ): String {
        log.warn("Call for NotifikasjonAdmin with adminEnabled: ${adminEnabled}")
        if(adminEnabled != "enabled") throw ResponseStatusException(HttpStatus.UNAUTHORIZED)

        log.info("NotifikasjonAdmin genererer varsel oppgave for uuid $uuid")

        varselPublisher.publish(uuid, fnr)
        return "OK"
    }

    @GetMapping("donemelding/{uuid}/{fnr}", produces = [ "text/plain" ])
    fun done(
            @PathVariable("uuid") uuid: UUID,
            @PathVariable("fnr") fnr: String
    ): String {
        log.warn("Call for NotifikasjonAdmin with adminEnabled: ${adminEnabled}")

        if(adminEnabled != "enabled") throw ResponseStatusException(HttpStatus.UNAUTHORIZED)

        log.info("NotifikasjonAdmin genererer done melding for uuid $uuid")

        varselPublisher.done(uuid, fnr)
        return "OK"
    }

}
