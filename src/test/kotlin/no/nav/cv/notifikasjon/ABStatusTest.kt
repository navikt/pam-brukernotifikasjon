package no.nav.cv.notifikasjon

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime

internal class ABStatusTest {
    private val now = ZonedDateTime.now()

    private fun nyBruker() = Status.nyBruker("dummy")

    @Test
    fun `nyBruker - AB Test Skal varsles - Riktig status`() {
        val nesteStatus = nyBruker().harKommetUnderOppfolging(now, ABTest.skalVarsles)

        assertThat(nesteStatus.status).isEqualTo(skalVarslesStatus)
    }

    @Test
    fun `nyBruker - AB Test Skal ikke varsles - Riktig status`() {
        val nesteStatus = nyBruker().harKommetUnderOppfolging(now, ABTest.skalIkkeVarsles )

        assertThat(nesteStatus.status).isEqualTo(abTestSkalIkkeVarsles)
    }
}