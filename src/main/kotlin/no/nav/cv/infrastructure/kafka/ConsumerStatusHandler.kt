package no.nav.cv.infrastructure.kafka

import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import java.util.concurrent.atomic.AtomicInteger


class ConsumerStatusHandler(
        private val consumers: List<Consumer<out Any, out Any>>
) {

    companion object {
        private val log = LoggerFactory.getLogger(ConsumerStatusHandler::class.java)
    }

    private val unhealthy: Map<String, AtomicInteger> = consumers
            .map { it.topic to AtomicInteger(0) }
            .toMap()

    @EventListener(ApplicationReadyEvent::class)
    fun startUp() = consumers.forEach { it.startPolling() }

    @Scheduled(fixedRate = 60000)
    fun checkForStoppedConsumers() {
        consumers.forEach {
            if(it.isStopped()) {
                log.error("${it.topic} har stoppet - forsøker å restarte")
                unhealthy.getValue(it.topic).getAndAdd(1)
                restart(it)
            }
            else {
                unhealthy.getValue(it.topic).set(0)
            }

        }
    }

    // TODO Attach to isAlive to restart? Or isReady to remove traffic?
    fun isUnhealthy() : Boolean {
        return unhealthy.any { it.value.get() > 10 }
                .also { isUnhealthy -> if(isUnhealthy) log.error("Some consumers are unhealthy") }
    }

    private fun restart(consumer: Consumer<out Any, out Any>) = consumer.startPolling()


}
