package no.nav.cv.infrastructure.admin

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import no.nav.cv.person.PersonIdent
import no.nav.cv.person.PersonIdentRepository
import no.nav.cv.person.PersonIdenter

@Controller("internal")
class PersonIdentAdmin(
        private val personIdentRepository: PersonIdentRepository
) {

    private val personIdentAdminEnabled = "\${personIdentAdminEnabled}"

    @Get("addIdent/{fnr}/{aktorId}")
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