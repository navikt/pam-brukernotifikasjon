package no.nav.cv.notifikasjon

import io.mockk.called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.cv.person.PersonIdent
import no.nav.cv.person.PersonIdentRepository
import no.nav.cv.person.PersonIdenter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime

internal class StatusTest {
    val aktor = "dummy"
    val aktorFnr = "dummy_fnr"

    val personIdenterAktorId = PersonIdenter(listOf(
            PersonIdent(aktor, PersonIdent.Type.AKTORID, true),
            PersonIdent(aktorFnr, PersonIdent.Type.FOLKEREGISTER, true)
    ))

    val varselPublisher = mockk<VarselPublisher>(relaxed = true)

    val personIdentRepository = mockk <PersonIdentRepository>(relaxed = true)


    private val now = ZonedDateTime.now()
    private val yesterday = ZonedDateTime.now().minusDays(1)
    private val twoDaysAgo = ZonedDateTime.now().minusDays(2)
    
    @Test
    fun `ny bruker`() {
        val ny = nyBruker()
        assertEquals(ny.status, nyBrukerStatus)
    }

    @Test
    fun `ny bruker - varsle bruker - gir uendret status`() {
        val ny = nyBruker()
        val forsoktVarslet = ny.varsleBruker(varselPublisher)

        verify { listOf(varselPublisher) wasNot called }
        assertEquals(forsoktVarslet.status, ny.status)
    }

    @Test
    fun `ny bruker - kommet under oppfolging - gir status skal varsles`() {
        val ny = nyBruker()
        val underOppfolging = ny.harKommetUnderOppfolging(twoDaysAgo, ABTest.skalVarsles)

        assertEquals(underOppfolging.status, skalVarslesStatus)
        assertEquals(underOppfolging.statusTidspunkt, twoDaysAgo)
    }

    @Test
    fun `ny bruker - sett cv - gir status done`() {
        val ny = nyBruker()
        val settCv = ny.harSettCv(twoDaysAgo, varselPublisher)

        verify { varselPublisher wasNot called }
        assertEquals(settCv.status, doneStatus)
        assertEquals(settCv.statusTidspunkt, twoDaysAgo)
    }

    @Test
    fun `ny bruker - blitt fulgt opp - gir status done`() {
        val ny = nyBruker()
        val blittFulgtOpp = ny.ikkeUnderOppfølging(twoDaysAgo)

        verify { varselPublisher wasNot called }
        assertEquals(blittFulgtOpp.status, doneStatus)
        assertEquals(blittFulgtOpp.statusTidspunkt, twoDaysAgo)
    }


    @Test
    fun `bruker fullfort oppfolging`() {
        val fulgtOpp = fulgtOppStatus()
        assertEquals(fulgtOpp.status, doneStatus)
    }

    @Test
    fun `bruker fullfort oppfolging - varsle bruker - gir uendret status`() {
        val fulgtOpp = fulgtOppStatus()
        val forsoktVarslet = fulgtOpp.varsleBruker(varselPublisher)

        verify { listOf(varselPublisher) wasNot called }
        assertEquals(forsoktVarslet.status, fulgtOpp.status)
    }

    @Test
    fun `bruker fullfort oppfolging - kommet under oppfolging - gir status skal varsles`() {
        val fulgtOpp = fulgtOppStatus()
        val underOppfolging = fulgtOpp.harKommetUnderOppfolging(twoDaysAgo, ABTest.skalVarsles)

        assertEquals(underOppfolging.status, skalVarslesStatus)
        assertEquals(underOppfolging.statusTidspunkt, twoDaysAgo)
    }

    @Test
    fun `bruker fullfort oppfolging - sett cv - gir status done`() {
        val fulgtOpp = fulgtOppStatus()
        val settCv = fulgtOpp.harSettCv(twoDaysAgo, varselPublisher)

        verify { varselPublisher wasNot called }
        assertEquals(settCv.status, doneStatus)
        assertEquals(settCv.statusTidspunkt, twoDaysAgo)
    }

    @Test
    fun `bruker fullfort oppfolging - blitt fulgt opp - gir status done`() {
        val fulgtOpp = fulgtOppStatus()
        val blittFulgtOpp = fulgtOpp.ikkeUnderOppfølging(twoDaysAgo)

        verify { varselPublisher wasNot called }
        assertEquals(blittFulgtOpp.status, doneStatus)
        assertEquals(blittFulgtOpp.statusTidspunkt, twoDaysAgo)
    }






    @Test
    fun `bruker sett cv`() {
        val settCv = settCv()
        assertEquals(settCv.status, doneStatus)
    }

    @Test
    fun `bruker sett cv - varsle bruker - gir uendret status`() {
        val settCv = settCv()
        val forsoktVarslet = settCv.varsleBruker(varselPublisher)

        assertEquals(forsoktVarslet.status, settCv.status)
    }

    @Test
    fun `bruker sett cv - kommet under oppfolging - gir status skal varsles`() {
        val settCv = settCv()
        val underOppfolging = settCv.harKommetUnderOppfolging(twoDaysAgo, ABTest.skalVarsles)

        assertEquals(underOppfolging.status, skalVarslesStatus)
        assertEquals(underOppfolging.statusTidspunkt, twoDaysAgo)
    }

    @Test
    fun `bruker sett cv - sett cv - gir status uendret`() {
        val settCv = settCv()
        val settCvNy = settCv.harSettCv(twoDaysAgo, varselPublisher)

        assertEquals(settCvNy.status, doneStatus)
        assertEquals(settCvNy.statusTidspunkt, settCv.statusTidspunkt)
    }

    @Test
    fun `bruker sett cv - blitt fulgt opp - gir uendret status`() {
        val settCv = settCv()
        val blittFulgtOpp = settCv.ikkeUnderOppfølging(twoDaysAgo)

        assertEquals(blittFulgtOpp.status, settCv.status)
        assertEquals(blittFulgtOpp.statusTidspunkt, settCv.statusTidspunkt)
    }








    @Test
    fun `bruker under oppfolging`() {
        val kommetUnderOppfolging = kommetUnderOppfolging()
        assertEquals(kommetUnderOppfolging.status, skalVarslesStatus)
    }

    @Test
    fun `bruker under oppfolging - varsle bruker - varsel publiseres nar det finnes fodselsnummer`() {
        every { personIdentRepository.finnIdenter(aktor) } returns personIdenterAktorId

        val kommetUnderOppfolgingMedFnr = kommetUnderOppfolgingMedFnr()
        val forsoktVarsletSuksess = kommetUnderOppfolgingMedFnr.varsleBruker(varselPublisher)


        verify(exactly = 1) { varselPublisher.publish(kommetUnderOppfolgingMedFnr.uuid, aktorFnr) }
        assertEquals(forsoktVarsletSuksess.status, varsletStatus)
    }

    @Test
    fun `bruker under oppfolging - kommet under oppfolging - gir status uendret men oppdaterer dato med ny dato`() {
        val kommetUnderOppfolging = kommetUnderOppfolging()
        val underOppfolging = kommetUnderOppfolging.harKommetUnderOppfolging(yesterday, ABTest.skalVarsles)

        assertEquals(underOppfolging.status, skalVarslesStatus)
        assertEquals(underOppfolging.statusTidspunkt, yesterday)
    }

    @Test
    fun `bruker under oppfolging - sett cv - gir status done`() {
        val kommetUnderOppfolging = kommetUnderOppfolging()
        val settCv = kommetUnderOppfolging.harSettCv(twoDaysAgo, varselPublisher)

        verify { varselPublisher wasNot called }
        assertEquals(settCv.status, doneStatus)
        assertEquals(settCv.statusTidspunkt, twoDaysAgo)
    }

    @Test
    fun `bruker under oppfolging - blitt fulgt opp - gir status done`() {
        val kommetUnderOppfolging = kommetUnderOppfolging()
        val blittFulgtOpp = kommetUnderOppfolging.ikkeUnderOppfølging(twoDaysAgo)

        verify { varselPublisher wasNot called }
        assertEquals(blittFulgtOpp.status, doneStatus)
        assertEquals(blittFulgtOpp.statusTidspunkt, twoDaysAgo)
    }





    @Test
    fun `bruker varslet`() {
        val varslet = varsletStatus()
        assertEquals(varslet.status, varsletStatus)
    }


    @Test
    fun `bruker varslet - varsle bruker - gir uendret status`() {
        val varslet = varsletStatus()
        val nyVarslet = varslet.varsleBruker(varselPublisher)


        verify { listOf(varselPublisher) wasNot called }
        assertEquals(nyVarslet.status, varslet.status)
    }

    @Test
    fun `bruker varslet - kommet under oppfolging - gir uendret status`() {
        val varslet = varsletStatus()
        val underOppfolging = varslet.harKommetUnderOppfolging(yesterday, ABTest.skalVarsles)

        assertEquals(underOppfolging.status, varslet.status)
    }

    @Test
    fun `bruker varslet - sett cv - gir status done og done publiseres`() {
        val varslet = varsletStatus()
        val settCv = varslet.harSettCv(now, varselPublisher)

        verify(exactly = 1) { varselPublisher.done(varslet.uuid, aktorFnr) }
        assertEquals(settCv.status, doneStatus)
        assertEquals(settCv.statusTidspunkt, now)
    }

    @Test
    fun `bruker varslet - blitt fulgt opp - gir status done og done publiseres`() {
        val varslet = varsletStatus()
        val blittFulgtOpp = varslet.ikkeUnderOppfølging(now)

        verify(exactly = 1) { varselPublisher.done(varslet.uuid, aktorFnr) }
        assertEquals(blittFulgtOpp.status, doneStatus)
        assertEquals(blittFulgtOpp.statusTidspunkt, now)
    }


    private fun nyBruker() = Status.nySession("dummy")

    fun fulgtOppStatus() = nyBruker().ikkeUnderOppfølging(twoDaysAgo)

    fun settCv() = varsletStatus().harSettCv(twoDaysAgo, varselPublisher)

    fun kommetUnderOppfolging(): Status = nyBruker().harKommetUnderOppfolging(twoDaysAgo, ABTest.skalVarsles)

    fun kommetUnderOppfolgingMedFnr(): Status = nyBruker().harKommetUnderOppfolging(twoDaysAgo, ABTest.skalVarsles)
            .funnetFodselsnummer(aktorFnr)

    fun varsletStatus(): Status {
        val varselPublisher = mockk<VarselPublisher>(relaxed = true)
        val personIdentRepository = mockk <PersonIdentRepository>(relaxed = true)
        every { personIdentRepository.finnIdenter(aktor) } returns personIdenterAktorId
        return nyBruker().harKommetUnderOppfolging(twoDaysAgo, ABTest.skalVarsles)
                .funnetFodselsnummer(aktorFnr)
                .varsleBruker(varselPublisher)
    }

}