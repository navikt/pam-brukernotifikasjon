package no.nav.cv.notifikasjon

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class VarslerJobb(
        private val hendelseService: HendelseService,
        private val repository: StatusRepository
) {

    companion object {
        val log = LoggerFactory.getLogger(VarslerJobb::class.java)
    }

    @SchedulerLock(name = "varslerjobb")
    @Scheduled(fixedDelay = 30 * 1000)
    fun varsle() {
        val skalVarsles = repository.skalVarsles()
        skalVarsles.forEach {
            log.debug("Varsler uuid ${it.uuid} med status ${it.status} og nåværende tidspunkt ${it.statusTidspunkt}")
            hendelseService.varsleBruker(it.aktoerId)
        }
    }
}
