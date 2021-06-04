package no.nav.cv.infrastructure.metrics

import io.micrometer.core.instrument.MeterRegistry
import no.nav.cv.notifikasjon.Status
import no.nav.cv.notifikasjon.StatusRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import java.time.ZonedDateTime

@SpringBootTest
internal class GenerateMetricsTest {


    private lateinit var statusRepository: StatusRepository
    private lateinit var  generateMetrics: GenerateMetrics
    private lateinit var  meterRegistry: MeterRegistry

    @Test
    fun `create some varslet status - ensure each is counted`() {
        val aidA = "123"
        val fnrA = "321"

        val nyBrukerStatusA = Status.nyBruker(aidA)
        val varsletStatusA = Status.varslet(
                forrigeStatus = nyBrukerStatusA,
                fnr = fnrA,
                statusTidspunkt = ZonedDateTime.now())

        statusRepository.lagre(varsletStatusA)

        val tmpA = statusRepository.finnSiste(aidA)

        assertEquals("varslet", tmpA.status)

        generateMetrics.generateMetrics()

        val antallVarsletA = meterRegistry.get("cv.brukernotifikasjon.status.varslet").gauge().value()

        assertEquals(1.0, antallVarsletA)
    }


}
