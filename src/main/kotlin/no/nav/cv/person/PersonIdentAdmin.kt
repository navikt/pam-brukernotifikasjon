package no.nav.cv.person

import io.micronaut.context.annotation.Value
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import no.nav.cv.person.PersonIdent
import no.nav.cv.person.PersonIdentRepository
import no.nav.cv.person.PersonIdenter

@Controller("internal")
class PersonIdentAdmin(
        @Value("\${personIdent.admin.enabled}") private val personIdentAdminEnabled: String,
        private val personIdentRepository: PersonIdentRepository
) {

    @Get("addIdent/{fnr}/{aktorId}", produces = [ "text/plain" ])
    fun addIdent(fnr: String, aktorId: String) : String {

        check(personIdentAdminEnabled == "enabled")

        val identList = listOf(
                PersonIdent(fnr, PersonIdent.Type.FOLKEREGISTER, true),
                PersonIdent(aktorId, PersonIdent.Type.AKTORID, true)
        )

        val identer = PersonIdenter(identList)

        personIdentRepository.oppdater(identer)

        return "OK"
    }
}