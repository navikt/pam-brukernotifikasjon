package no.nav.cv.infrastructure.metrics

import io.micrometer.core.instrument.MeterRegistry
import no.nav.cv.notifikasjon.StatusRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicLong

@Service
class GenerateMetrics(
    private val meterRegistry: MeterRegistry,
    private val statusRepository: StatusRepository
) {

    val gauges = mutableMapOf<String, AtomicLong?>()

    private fun addOrUpdateGauge(name: String, value: Long) {
        gauges[name]?.set(value)
            ?: addGauge(name, value)
    }

    private fun addGauge(name: String, value: Long) {
        gauges[name] = meterRegistry.gauge(name, AtomicLong(value))
    }

    @Scheduled(fixedDelay = 5*60*1000) // Every 5 minutes
    fun generateMetrics() {

        val antallVarslet = statusRepository.antallAvStatus("varslet")
        addOrUpdateGauge("cv.brukernotifikasjon.status.varslet", antallVarslet)

        val antallFerdig = statusRepository.antallAvStatus("done")
        addOrUpdateGauge("cv.brukernotifikasjon.status.ferdig", antallFerdig)

    }
}
