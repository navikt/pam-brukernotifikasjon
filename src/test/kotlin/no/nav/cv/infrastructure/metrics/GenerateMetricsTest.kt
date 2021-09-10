package no.nav.cv.infrastructure.metrics

import io.micrometer.core.instrument.MeterRegistry
import no.nav.cv.notifikasjon.Status
import no.nav.cv.notifikasjon.StatusRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.ZonedDateTime

@Disabled
@SpringBootTest
internal class GenerateMetricsTest {


    @Autowired
    private lateinit var statusRepository: StatusRepository

    @Autowired
    private lateinit var  generateMetrics: GenerateMetrics

    @Autowired
    private lateinit var  meterRegistry: MeterRegistry

    @Test
    fun `create some varslet status - ensure each is counted`() {
        val aidA = "123"
        val fnrA = "321"

        val skalVarslesStatus = Status.nySession(aidA).skalVarlsesManglerFnr(ZonedDateTime.now())
        val varsletStatusA = Status.varslet(
                forrigeStatus = skalVarslesStatus,
                foedselsnummer = fnrA,
                statusTidspunkt = ZonedDateTime.now())

        statusRepository.lagreNyStatus(varsletStatusA)

        val tmpA = statusRepository.finnSiste(aidA)

        assertEquals("varslet", tmpA.status)

        generateMetrics.generateMetrics()

        val antallVarsletA = meterRegistry.get("cv.brukernotifikasjon.status.varslet").gauge().value()

        assertEquals(1.0, antallVarsletA)
    }


}
