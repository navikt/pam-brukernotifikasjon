package no.nav.cv.notifikasjon

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime

internal class ABStatusTest {
    private val now = ZonedDateTime.now()

    private fun nyBruker() = Status.nySession("dummy")

    @Test
    fun `nyBruker - AB Test Skal varsles - Riktig status`() {
        val nesteStatus = nyBruker().harKommetUnderOppfolging(now, ABTest.skalVarsles)

        assertEquals(nesteStatus.status, skalVarslesStatus)
    }

    @Test
    fun `nyBruker - AB Test Skal ikke varsles - Riktig status`() {
        val nesteStatus = nyBruker().harKommetUnderOppfolging(now, ABTest.skalIkkeVarsles )

        assertEquals(nesteStatus.status, abTestSkalIkkeVarsles)
    }
}