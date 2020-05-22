package no.nav.cv.notifikasjon

import io.micronaut.scheduling.annotation.Scheduled
import no.nav.cv.person.PersonIdentRepository
import javax.inject.Singleton

@Singleton
class VarslerJobb(
        hendelseService: HendelseService,
        repository: PersonIdentRepository
) {

    @Scheduled(fixedDelay = "15s")
    fun varsle() {

    }
}