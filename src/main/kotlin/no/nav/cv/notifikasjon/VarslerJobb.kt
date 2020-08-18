package no.nav.cv.notifikasjon

import io.micronaut.scheduling.annotation.Scheduled
import net.javacrumbs.shedlock.micronaut.SchedulerLock
import org.slf4j.LoggerFactory
import java.time.ZonedDateTime
import javax.inject.Singleton

@Singleton
open class VarslerJobb(
        private val hendelseService: HendelseService,
        private val repository: StatusRepository
) {

    companion object {
        val log = LoggerFactory.getLogger(VarslerJobb::class.java)
    }

    @SchedulerLock(name = "varslerjobb")
    @Scheduled(fixedDelay = "15s")
    open fun varsle() {
        val skalVarsles = repository.skalVarsles()
        skalVarsles.forEach {
            log.debug("Varsler uuid ${it.uuid} med status ${it.status} og nåværende tidspunkt ${it.statusTidspunkt}")
            hendelseService.varsleBruker(it.aktorId)
        }
    }
}