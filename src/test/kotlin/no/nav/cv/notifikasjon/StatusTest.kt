package no.nav.cv.notifikasjon

import com.ninjasquad.springmockk.MockkBean
import io.mockk.called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.cv.personident.PersonOppslag
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.ZonedDateTime

@ExtendWith(SpringExtension::class)
internal class StatusTest {
    val aktor = "dummy"
    val aktorFnr = "dummy_fnr"

    @MockkBean(PersonOppslag::class)
    private lateinit var personOppslag: PersonOppslag

    @BeforeEach
    fun setupPersonoppslag() {
        every { personOppslag.hentAktorId(aktorFnr) } returns aktor
        every { personOppslag.hentAktorId(aktor) } returns aktorFnr
    }


    val varselPublisher = mockk<VarselPublisher>(relaxed = true)

    private val now = ZonedDateTime.now()
    private val yesterday = ZonedDateTime.now().minusDays(1)
    private val twoDaysAgo = ZonedDateTime.now().minusDays(2)


    @Test
    fun `ny bruker`() {
        val ny = nyBruker()
        assertEquals(ny.status, nyBrukerStatus)
    }

    @Test
    fun `ny bruker - kommet under oppfolging - gir status mangler fødselsnummer`() {
        val ny = nyBruker()
        val underOppfolging = ny.skalVarlsesManglerFnr(twoDaysAgo)

        assertEquals(underOppfolging.status, skalVarslesManglerFnrStatus)
        assertEquals(underOppfolging.statusTidspunkt, twoDaysAgo)
    }

    @Test
    fun `ny bruker - sett cv - gir status cv oppdatert`() {
        val ny = nyBruker()
        val settCv = ny.endretCV(twoDaysAgo)

        assertEquals(settCv.status, cvOppdatertStatus)
        assertEquals(settCv.statusTidspunkt, twoDaysAgo)
    }

    @Test
    fun `ny bruker - blitt fulgt opp - gir status ikke under oppfølging`() {
        val ny = nyBruker()
        val blittFulgtOpp = ny.ikkeUnderOppfølging(twoDaysAgo)

        assertEquals(blittFulgtOpp.status, ikkeUnderOppfølgingStatus)
        assertEquals(blittFulgtOpp.statusTidspunkt, twoDaysAgo)
    }


    @Test
    fun `bruker fullfort oppfolging`() {
        val fulgtOpp = ikkeUnderOppfolging()
        assertEquals(fulgtOpp.status, doneStatus)
    }

//    @Test
//    fun `bruker fullfort oppfolging - varsle bruker - gir uendret status`() {
//        val fulgtOpp = ikkeUnderOppfolging()
//        val forsoktVarslet = fulgtOpp.varsleBruker(varselPublisher)
//
//        verify { listOf(varselPublisher) wasNot called }
//        assertEquals(forsoktVarslet.status, fulgtOpp.status)
//    }

//    @Test
//    fun `bruker fullfort oppfolging - kommet under oppfolging - gir status skal varsles`() {
//        val fulgtOpp = ikkeUnderOppfolging()
//        val underOppfolging = fulgtOpp.harKommetUnderOppfolging(twoDaysAgo, ABTest.skalVarsles)
//
//        assertEquals(underOppfolging.status, skalVarslesStatus)
//        assertEquals(underOppfolging.statusTidspunkt, twoDaysAgo)
//    }

//    @Test
//    fun `bruker fullfort oppfolging - sett cv - gir status done`() {
//        val fulgtOpp = ikkeUnderOppfolging()
//        val settCv = fulgtOpp.harSettCv(twoDaysAgo, varselPublisher)
//
//        verify { varselPublisher wasNot called }
//        assertEquals(settCv.status, doneStatus)
//        assertEquals(settCv.statusTidspunkt, twoDaysAgo)
//    }

    @Test
    fun `bruker fullfort oppfolging - blitt fulgt opp - gir status done`() {
        val fulgtOpp = ikkeUnderOppfolging()
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

//    @Test
//    fun `bruker sett cv - varsle bruker - gir uendret status`() {
//        val settCv = settCv()
//        val forsoktVarslet = settCv.varsleBruker(varselPublisher)
//
//        assertEquals(forsoktVarslet.status, settCv.status)
//    }

//    @Test
//    fun `bruker sett cv - kommet under oppfolging - gir status skal varsles`() {
//        val settCv = settCv()
//        val underOppfolging = settCv.harKommetUnderOppfolging(twoDaysAgo, ABTest.skalVarsles)
//
//        assertEquals(underOppfolging.status, skalVarslesStatus)
//        assertEquals(underOppfolging.statusTidspunkt, twoDaysAgo)
//    }

//    @Test
//    fun `bruker sett cv - sett cv - gir status uendret`() {
//        val settCv = settCv()
//        val settCvNy = settCv.harSettCv(twoDaysAgo, varselPublisher)
//
//        assertEquals(settCvNy.status, doneStatus)
//        assertEquals(settCvNy.statusTidspunkt, settCv.statusTidspunkt)
//    }

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

//    @Test
//    fun `bruker under oppfolging - varsle bruker - varsel publiseres nar det finnes fodselsnummer`() {
//        val kommetUnderOppfolgingMedFnr = kommetUnderOppfolgingMedFnr()
//        val forsoktVarsletSuksess = kommetUnderOppfolgingMedFnr.varsleBruker(varselPublisher)
//
//
//        verify(exactly = 1) { varselPublisher.publish(kommetUnderOppfolgingMedFnr.uuid, aktorFnr) }
//        assertEquals(forsoktVarsletSuksess.status, varsletStatus)
//    }

//    @Test
//    fun `bruker under oppfolging - kommet under oppfolging - gir status uendret men oppdaterer dato med ny dato`() {
//        val kommetUnderOppfolging = kommetUnderOppfolging()
//        val underOppfolging = kommetUnderOppfolging.harKommetUnderOppfolging(yesterday, ABTest.skalVarsles)
//
//        assertEquals(underOppfolging.status, skalVarslesStatus)
//        assertEquals(underOppfolging.statusTidspunkt, yesterday)
//    }

//    @Test
//    fun `bruker under oppfolging - sett cv - gir status done`() {
//        val kommetUnderOppfolging = kommetUnderOppfolging()
//        val settCv = kommetUnderOppfolging.harSettCv(twoDaysAgo, varselPublisher)
//
//        verify { varselPublisher wasNot called }
//        assertEquals(settCv.status, doneStatus)
//        assertEquals(settCv.statusTidspunkt, twoDaysAgo)
//    }

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


//    @Test
//    fun `bruker varslet - varsle bruker - gir uendret status`() {
//        val varslet = varsletStatus()
//        val nyVarslet = varslet.varsleBruker(varselPublisher)
//
//
//        verify { listOf(varselPublisher) wasNot called }
//        assertEquals(nyVarslet.status, varslet.status)
//    }

//    @Test
//    fun `bruker varslet - kommet under oppfolging - gir uendret status`() {
//        val varslet = varsletStatus()
//        val underOppfolging = varslet.harKommetUnderOppfolging(yesterday, ABTest.skalVarsles)
//
//        assertEquals(underOppfolging.status, varslet.status)
//    }

//    @Test
//    fun `bruker varslet - sett cv - gir status done og done publiseres`() {
//        val varslet = varsletStatus()
//        val settCv = varslet.harSettCv(now, varselPublisher)
//
//        verify(exactly = 1) { varselPublisher.done(varslet.uuid, aktorFnr) }
//        assertEquals(settCv.status, doneStatus)
//        assertEquals(settCv.statusTidspunkt, now)
//    }

    @Test
    fun `bruker varslet - blitt fulgt opp - gir status done og done publiseres`() {
        val varslet = varsletStatus()
        val blittFulgtOpp = varslet.ikkeUnderOppfølging(now)

        verify(exactly = 1) { varselPublisher.done(varslet.uuid, aktorFnr) }
        assertEquals(blittFulgtOpp.status, doneStatus)
        assertEquals(blittFulgtOpp.statusTidspunkt, now)
    }


    private fun nyBruker() = Status.nySession("dummy")

    fun ikkeUnderOppfolging() = nyBruker().ikkeUnderOppfølging(twoDaysAgo)

    fun settCv() = varsletStatus().endretCV(twoDaysAgo)

    fun kommetUnderOppfolging(): Status = nyBruker().skalVarlsesManglerFnr(twoDaysAgo)

    fun kommetUnderOppfolgingMedFnr(): Status = nyBruker().skalVarsles(aktorFnr)

    fun varsletStatus(): Status {
        val varselPublisher = mockk<VarselPublisher>(relaxed = true)
        return Status.varslet(
            nyBruker()
                .skalVarlsesManglerFnr(twoDaysAgo)
                .skalVarsles(aktorFnr),
            now)

    }

}