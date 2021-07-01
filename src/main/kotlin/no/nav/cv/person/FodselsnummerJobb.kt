package no.nav.cv.person

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import no.nav.cv.notifikasjon.StatusRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service


@Service
class FodselsnummerJobb(
        private val statusRepository: StatusRepository,
        private val personIdentRepository: PersonIdentRepository
) {

    @SchedulerLock(name = "fodselsnummerjobb")
    @Scheduled(fixedDelay = 5 * 60 * 1000)
    fun fyllInnFnr() {
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
