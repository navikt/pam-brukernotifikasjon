package no.nav.cv.notifikasjon

import io.micronaut.context.annotation.Value
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import java.util.*


@Controller("internal")
class NotifikasjonAdmin(
        @Value("\${personIdent.admin.enabled}") private val personIdentAdminEnabled: String,
        private val statusRepository: StatusRepository,
        private val varselPublisher: VarselPublisher
) {

    @Post("kafka/manuell/varsel/{uuid}/{fnr}", produces = [ "text/plain" ])
    fun varsle(uuid: UUID, fnr: String): String {
        varselPublisher.publish(uuid, fnr)
        return "OK"
    }

    @Post("kafka/manuell/donemelding/{uuid}/{fnr}", produces = [ "text/plain" ])
    fun done(uuid: UUID, fnr: String): String {
        varselPublisher.done(uuid, fnr)
        return "OK"
    }

    @Post("kafka/manuell/donemelding/{uuid}/{fnr}/{systembruker}", produces = [ "text/plain" ])
    fun doneMedSystembruker(uuid: UUID, fnr: String, systembruker: String): String {
        varselPublisher.done(uuid, fnr, systembruker)
        return "OK"
    }

}