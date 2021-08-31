package no.nav.cv.personident

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import no.nav.cv.notifikasjon.HendelseService
import no.nav.cv.notifikasjon.StatusRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service


@Service
class FodselsnummerJobb(
    private val hendelseService: HendelseService,
    private val statusRepository: StatusRepository,
    private val personOppslag: PersonOppslag
) {
    companion object {
        private val log = LoggerFactory.getLogger(FodselsnummerJobb::class.java)
    }

    @SchedulerLock(name = "fodselsnummerjobb")
    @Scheduled(fixedDelay = 5 * 60 * 1000)
    fun fyllInnFnr() {
        statusRepository.manglerFodselsnummer()
            .also { log.info("Running FødselsnummerJobb with ${it.size} tasks")}
            .forEach {
                val fnr = personOppslag.hentFoedselsnummer(it.aktoerId)

                log.info("Got personnummer for aktorid ${it.aktoerId}: ${fnr != null}")
                fnr ?: return@forEach

                hendelseService.funnetFodselsnummer(it.aktoerId, fnr)
            }


    }
}
