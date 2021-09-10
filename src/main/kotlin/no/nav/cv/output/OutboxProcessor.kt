package no.nav.cv.output

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import no.nav.cv.notifikasjon.Hendelser
import no.nav.cv.notifikasjon.VarselPublisher
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import java.util.*

@Service
class OutboxProcessor(
    private val outboxService: OutboxService,
) {
    @Scheduled(fixedDelay = 30 * 1000)
    @SchedulerLock(name = "OutboxProcessor", lockAtLeastFor = "PT20S", lockAtMostFor = "PT5M")
    fun processOutbox() = outboxService.processQueue(noDelay = false)
}