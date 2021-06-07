package no.nav.cv.person

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("internal/ident/")
class PersonIdentAdmin(
        @Value("\${admin.enabled}") private val adminEnabled: String,
        private val personIdentRepository: PersonIdentRepository
) {

    @GetMapping("add/{fnr}/{aktorId}", produces = [ "text/plain" ])
    fun addIdent(
            @PathVariable("fnr") fnr: String,
            @PathVariable("aktorId") aktorId: String) : String {

        if(adminEnabled != "enabled") throw ResponseStatusException(HttpStatus.UNAUTHORIZED)

        val identList = listOf(
                PersonIdent(fnr, PersonIdent.Type.FOLKEREGISTER, true),
                PersonIdent(aktorId, PersonIdent.Type.AKTORID, true)
        )

        val identer = PersonIdenter(identList)

        personIdentRepository.oppdater(identer)

        return "OK"
    }
}
