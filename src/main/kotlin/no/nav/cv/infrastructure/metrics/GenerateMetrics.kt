package no.nav.cv.infrastructure.metrics

import io.micrometer.core.instrument.MeterRegistry
import no.nav.cv.notifikasjon.StatusRepository
import org.springframework.scheduling.annotation.Scheduled
import javax.inject.Singleton

@Singleton
class GenerateMetrics(
        private val meterRegistry: MeterRegistry,
        private val statusRepository: StatusRepository
) {

    @Scheduled(fixedDelay = 5*60*1000) // Every 5 minutes
    fun generateMetrics() {

        val antallVarslet = statusRepository.antallAvStatus("varslet")
        meterRegistry.gauge("cv.brukernotifikasjon.status.varslet", antallVarslet)

        val antallFerdig = statusRepository.antallAvStatus("done")
        meterRegistry.gauge("cv.brukernotifikasjon.status.ferdig", antallFerdig)


    }
}