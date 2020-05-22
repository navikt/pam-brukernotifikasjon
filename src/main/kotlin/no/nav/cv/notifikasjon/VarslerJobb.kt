package no.nav.cv.notifikasjon

import io.micronaut.scheduling.annotation.Scheduled
import no.nav.cv.person.PersonIdentRepository
import java.time.ZonedDateTime
import javax.inject.Singleton

@Singleton
class VarslerJobb(
        private val hendelseService: HendelseService,
        private val repository: StatusRepository
) {

    @Scheduled(fixedDelay = "15s")
    fun varsle() {
        val skalVarsles = repository.skalVarsles()
        skalVarsles.forEach {
            hendelseService.varsleBruker(it.aktorId, ZonedDateTime.now())
        }
    }
}