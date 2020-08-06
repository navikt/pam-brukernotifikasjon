package no.nav.cv.person

import io.micronaut.context.annotation.Value
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get

@Controller("internal")
class PersonIdentAdmin(
        @Value("\${admin.enabled}") private val adminEnabled: String,
        private val personIdentRepository: PersonIdentRepository
) {

    @Get("addIdent/{fnr}/{aktorId}", produces = [ "text/plain" ])
    fun addIdent(fnr: String, aktorId: String) : String {

        check(adminEnabled == "enabled")

        val identList = listOf(
                PersonIdent(fnr, PersonIdent.Type.FOLKEREGISTER, true),
                PersonIdent(aktorId, PersonIdent.Type.AKTORID, true)
        )

        val identer = PersonIdenter(identList)

        personIdentRepository.oppdater(identer)

        return "OK"
    }
}