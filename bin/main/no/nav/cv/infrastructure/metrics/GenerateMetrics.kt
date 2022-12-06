package no.nav.cv.infrastructure.metrics

import io.micrometer.core.instrument.MeterRegistry
import no.nav.cv.notifikasjon.*
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicLong

@Service
class GenerateMetrics(
    private val meterRegistry: MeterRegistry,
    private val statusRepository: StatusRepository
) {
    private val log = LoggerFactory.getLogger(GenerateMetrics::class.java)

    private val gauges = mutableMapOf<String, AtomicLong?>()

    @Scheduled(fixedDelay = 5*60*1000) // Every 5 minutes
    fun generateMetrics() {

        val trackThese = listOf(varsletStatus, skalVarslesManglerFnrStatus, forGammelStatus, cvOppdatertStatus, ikkeUnderOppfølgingStatus )

        trackThese.map { it to update(it) }
            .joinToString { (status, antall) -> "$status: $antall" }
            .let { log.info("Antall aktive av hver status: $it") }

        val antallFerdigCvOppdatert = statusRepository.antallFerdig(cvOppdatertStatus)
        addOrUpdateGauge("cv.brukernotifikasjon.ferdig.$cvOppdatertStatus", antallFerdigCvOppdatert)

        val antallFerdigIkkeUnderOppfolging = statusRepository.antallFerdig(ikkeUnderOppfølgingStatus)
        addOrUpdateGauge("cv.brukernotifikasjon.ferdig.$ikkeUnderOppfølgingStatus", antallFerdigIkkeUnderOppfolging)
    }

    private fun update(status: String) : Long {
        val antall = statusRepository.antallAvStatus(status)
        addOrUpdateGauge("cv.brukernotifikasjon.status.$status", antall)

        return antall
    }

    private fun addOrUpdateGauge(name: String, value: Long) {
        gauges[name]?.set(value)
            ?: addGauge(name, value)
    }

    private fun addGauge(name: String, value: Long) {
        gauges[name] = meterRegistry.gauge(name, AtomicLong(value))
    }
}
