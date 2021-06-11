package no.nav.cv.infrastructure.kafka

import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import org.springframework.scheduling.annotation.Scheduled
import java.util.concurrent.atomic.AtomicInteger


class ConsumerStatusMetrics(
        private val consumers: List<Consumer<out Any, out Any>>,
        private val meterRegistry: MeterRegistry

) {

    private val metricName = "pam-brukernotifikasjon_consumer_status"

    private val statusGauges: Map<String, AtomicInteger> = consumers
            .map { it.topic to meterRegistry.gauge(metricName, listOf(Tag.of("topic", it.topic)), AtomicInteger(0))!! }
            .toMap()


    @Scheduled(fixedRate = 60000)
    fun reportStatus() {
        consumers.forEach {
            if(it.isStopped()) statusGauges.getValue(it.topic).set(0) else statusGauges.getValue(it.topic).set(1)
        }
    }

}
