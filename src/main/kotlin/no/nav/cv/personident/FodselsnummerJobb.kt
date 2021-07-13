package no.nav.cv.personident

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import no.nav.cv.notifikasjon.StatusRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service


@Service
class FodselsnummerJobb(
        private val statusRepository: StatusRepository,
        private val personOppslag: PersonOppslag
) {
    companion object {
        private val log = LoggerFactory.getLogger(FodselsnummerJobb::class.java)
    }

    @SchedulerLock(name = "fodselsnummerjobb")
    @Scheduled(fixedDelay = 10 * 1000)
    fun fyllInnFnr() {
        statusRepository.manglerFodselsnummer()
                .forEach {
                    val fnr =  personOppslag.hentFoedselsnummer(it.aktorId)

                    log.info("Got personnummer for aktorid ${it.aktorId}: ${fnr != null}")
                    fnr ?: return@forEach

                    val nyStatus = it.funnetFodselsnummer(fnr)

                    statusRepository.lagre(nyStatus)
                }


    }
}
