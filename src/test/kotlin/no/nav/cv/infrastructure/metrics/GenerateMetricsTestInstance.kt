package no.nav.cv.infrastructure.metrics

import io.micrometer.core.instrument.MeterRegistry
import no.nav.cv.SingletonPostgresTestInstance
import no.nav.cv.notifikasjon.Status
import no.nav.cv.notifikasjon.StatusRepository
import no.nav.cv.notifikasjon.cvOppdatertStatus
import no.nav.cv.notifikasjon.ikkeUnderOppfølgingStatus
import no.nav.cv.notifikasjon.varsletStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.ZonedDateTime

internal class GenerateMetricsTestInstance : SingletonPostgresTestInstance() {

    @Autowired
    private lateinit var statusRepository: StatusRepository

    @Autowired
    private lateinit var generateMetrics: GenerateMetrics

    @Autowired
    private lateinit var meterRegistry: MeterRegistry

    @Test
    fun `create some varslet status - ensure each is counted`() {
        val aidA = "123"
        val fnrA = "321"

        val skalVarslesStatus = Status.nySession(aidA).skalVarlsesManglerFnr(ZonedDateTime.now())
        val varsletStatusA = Status.varslet(
            forrigeStatus = skalVarslesStatus,
            foedselsnummer = fnrA,
            statusTidspunkt = ZonedDateTime.now()
        )

        statusRepository.lagreNyStatus(varsletStatusA)

        val tmpA = statusRepository.finnSiste(aidA)

        assertEquals("varslet", tmpA.status)

        generateMetrics.generateMetrics()

        val antallVarsletA = meterRegistry.get("cv.brukernotifikasjon.status.varslet").gauge().value()

        assertEquals(1.0, antallVarsletA)
    }

    @Test
    fun `conversion rate`() {

        val ids = listOf(
            "123" to "321",
            "456" to "654",
            "789" to "987",
            "000" to "999",
            "111" to "888",
            "222" to "777"
        )

        val statuses = ids.map { (aid, fnr) ->
            Status
                .nySession(aid)
                .skalVarlsesManglerFnr(ZonedDateTime.now())
                .varslet(foedselsnummer = fnr, tidspunkt = ZonedDateTime.now())
        }
            .onEach { status ->
                statusRepository.lagreNyStatus(status)
            }

        statuses[1].endretCV(ZonedDateTime.now())
            .let { statusRepository.lagreNyStatus(it) }

        statuses[2].endretCV(ZonedDateTime.now())
            .let { statusRepository.lagreNyStatus(it) }

        statuses[3].ikkeUnderOppfølging(ZonedDateTime.now())
            .let { statusRepository.lagreNyStatus(it) }

        val res = statusRepository.conversionRateWeekly()

        assertTrue(res.any { it.status == varsletStatus && it.count == 3L })
        assertTrue(res.any { it.status == cvOppdatertStatus && it.count == 2L })
        assertTrue(res.any { it.status == ikkeUnderOppfølgingStatus && it.count == 1L })
    }

}
