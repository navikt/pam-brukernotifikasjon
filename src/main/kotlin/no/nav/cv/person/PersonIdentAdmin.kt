package no.nav.cv.person

import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController("internal")
class PersonIdentAdmin(
        @Value("\${admin.enabled}") private val adminEnabled: String,
        private val personIdentRepository: PersonIdentRepository
) {

    @GetMapping("addIdent/{fnr}/{aktorId}", produces = [ "text/plain" ])
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
