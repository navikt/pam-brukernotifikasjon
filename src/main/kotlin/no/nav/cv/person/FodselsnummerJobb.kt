package no.nav.cv.person

import io.micronaut.scheduling.annotation.Scheduled
import net.javacrumbs.shedlock.micronaut.SchedulerLock
import no.nav.cv.notifikasjon.HendelseService
import no.nav.cv.notifikasjon.StatusRepository
import no.nav.cv.notifikasjon.ukjentFnr
import javax.inject.Singleton


@Singleton
open class FodselsnummerJobb(
        private val hendelseService: HendelseService,
        private val statusRepository: StatusRepository,
        private val personIdentRepository: PersonIdentRepository
) {

    @SchedulerLock(name = "fodselsnummerjobb")
    @Scheduled(fixedDelay = "15s")
    open fun fyllInnFnr() {
        statusRepository.manglerFodselsnummer()
                .forEach {

                    val fnr = personIdentRepository.finnIdenter(it.aktoerId)
                            .gjeldende(PersonIdent.Type.FOLKEREGISTER) ?: ukjentFnr

                    hendelseService.funnetFodselsnummer(it.aktoerId, fnr)
                }


    }

}