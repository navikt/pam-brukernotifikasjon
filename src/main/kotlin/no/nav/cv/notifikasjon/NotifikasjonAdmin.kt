package no.nav.cv.notifikasjon

import io.micronaut.context.annotation.Value
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import org.slf4j.LoggerFactory
import java.util.*


@Controller("internal")
class NotifikasjonAdmin(
        @Value("\${admin.enabled}") private val adminEnabled: String,
        private val statusRepository: StatusRepository,
        private val varselPublisher: VarselPublisher
) {

    companion object {
        private val log = LoggerFactory.getLogger(NotifikasjonAdmin::class.java)
    }


    @Post("kafka/manuell/varsel/{uuid}/{fnr}", produces = [ "text/plain" ])
    fun varsle(uuid: UUID, fnr: String): String {

        check(adminEnabled == "enabled")

        log.info("NotifikasjonAdmin genererer varsel oppgave for uuid $uuid")

        varselPublisher.publish(uuid, fnr)
        return "OK"
    }

    @Post("kafka/manuell/donemelding/{uuid}/{fnr}", produces = [ "text/plain" ])
    fun done(uuid: UUID, fnr: String): String {

        check(adminEnabled == "enabled")

        log.info("NotifikasjonAdmin genererer done melding for uuid $uuid")

        varselPublisher.done(uuid, fnr)
        return "OK"
    }

}