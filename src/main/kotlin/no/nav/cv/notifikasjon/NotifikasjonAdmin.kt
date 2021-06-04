package no.nav.cv.notifikasjon

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*


@RestController("internal")
class NotifikasjonAdmin(
        @Value("\${admin.enabled}") private val adminEnabled: String,
        private val statusRepository: StatusRepository,
        private val varselPublisher: VarselPublisher
) {

    companion object {
        private val log = LoggerFactory.getLogger(NotifikasjonAdmin::class.java)
    }


    @PostMapping("kafka/manuell/varsel/{uuid}/{fnr}", produces = [ "text/plain" ])
    fun varsle(uuid: UUID, fnr: String): String {

        check(adminEnabled == "enabled")

        log.info("NotifikasjonAdmin genererer varsel oppgave for uuid $uuid")

        varselPublisher.publish(uuid, fnr)
        return "OK"
    }

    @PostMapping("kafka/manuell/donemelding/{uuid}/{fnr}", produces = [ "text/plain" ])
    fun done(uuid: UUID, fnr: String): String {

        check(adminEnabled == "enabled")

        log.info("NotifikasjonAdmin genererer done melding for uuid $uuid")

        varselPublisher.done(uuid, fnr)
        return "OK"
    }

}
