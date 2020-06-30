package no.nav.cv.person

import io.micronaut.scheduling.annotation.Scheduled
import net.javacrumbs.shedlock.micronaut.SchedulerLock
import no.nav.cv.notifikasjon.StatusRepository
import javax.inject.Singleton


@Singleton
open class FodselsnummerJobb(
        private val statusRepository: StatusRepository,
        private val personIdentRepository: PersonIdentRepository
) {

    @SchedulerLock(name = "fodselsnummerjobb")
    @Scheduled(fixedDelay = "15s")
    open fun fyllInnFnr() {
        statusRepository.manglerFodselsnummer()
                .forEach {
                    val fnr = personIdentRepository.finnIdenter(it.aktorId)
                            .gjeldende(PersonIdent.Type.FOLKEREGISTER)

                    fnr ?: return@forEach

                    val nyStatus = it.funnetFodselsnummer(fnr)

                    statusRepository.lagre(nyStatus)
                }


    }
}