package no.nav.cv.output

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class OutboxProcessor(
    private val outboxService: OutboxService,
) {
    @Scheduled(fixedDelay = 30 * 1000)
    @SchedulerLock(name = "OutboxProcessor", lockAtLeastFor = "PT20S", lockAtMostFor = "PT5M")
    fun processOutbox() = outboxService.processQueue(noDelay = false)
}