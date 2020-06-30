package no.nav.cv.notifikasjon

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import io.mockk.called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.cv.person.PersonIdent
import no.nav.cv.person.PersonIdentRepository
import no.nav.cv.person.PersonIdenter
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
        assertThat(ny.status).isEqualTo(nyBrukerStatus)
    }

    @Test
    fun `ny bruker - varsle bruker - gir uendret status`() {
        val ny = nyBruker()
        val forsoktVarslet = ny.varsleBruker(varselPublisher)

        verify { listOf(varselPublisher) wasNot called }
        assertThat(forsoktVarslet.status).isEqualTo(ny.status)
    }

    @Test
    fun `ny bruker - kommet under oppfolging - gir status skal varsles`() {
        val ny = nyBruker()
        val underOppfolging = ny.harKommetUnderOppfolging(twoDaysAgo)

        assertThat(underOppfolging.status).isEqualTo(skalVarslesStatus)
        assertThat(underOppfolging.statusTidspunkt).isEqualTo(twoDaysAgo)
    }

    @Test
    fun `ny bruker - sett cv - gir status done`() {
        val ny = nyBruker()
        val settCv = ny.harSettCv(twoDaysAgo, varselPublisher)

        verify { varselPublisher wasNot called }
        assertThat(settCv.status).isEqualTo(doneStatus)
        assertThat(settCv.statusTidspunkt).isEqualTo(twoDaysAgo)
    }

    @Test
    fun `ny bruker - blitt fulgt opp - gir status done`() {
        val ny = nyBruker()
        val blittFulgtOpp = ny.blittFulgtOpp(twoDaysAgo, varselPublisher)

        verify { varselPublisher wasNot called }
        assertThat(blittFulgtOpp.status).isEqualTo(doneStatus)
        assertThat(blittFulgtOpp.statusTidspunkt).isEqualTo(twoDaysAgo)
    }


    @Test
    fun `bruker fullfort oppfolging`() {
        val fulgtOpp = fulgtOppStatus()
        assertThat(fulgtOpp.status).isEqualTo(doneStatus)
    }

    @Test
    fun `bruker fullfort oppfolging - varsle bruker - gir uendret status`() {
        val fulgtOpp = fulgtOppStatus()
        val forsoktVarslet = fulgtOpp.varsleBruker(varselPublisher)

        verify { listOf(varselPublisher) wasNot called }
        assertThat(forsoktVarslet.status).isEqualTo(fulgtOpp.status)
    }

    @Test
    fun `bruker fullfort oppfolging - kommet under oppfolging - gir status skal varsles`() {
        val fulgtOpp = fulgtOppStatus()
        val underOppfolging = fulgtOpp.harKommetUnderOppfolging(twoDaysAgo)

        assertThat(underOppfolging.status).isEqualTo(skalVarslesStatus)
        assertThat(underOppfolging.statusTidspunkt).isEqualTo(twoDaysAgo)
    }

    @Test
    fun `bruker fullfort oppfolging - sett cv - gir status done`() {
        val fulgtOpp = fulgtOppStatus()
        val settCv = fulgtOpp.harSettCv(twoDaysAgo, varselPublisher)

        verify { varselPublisher wasNot called }
        assertThat(settCv.status).isEqualTo(doneStatus)
        assertThat(settCv.statusTidspunkt).isEqualTo(twoDaysAgo)
    }

    @Test
    fun `bruker fullfort oppfolging - blitt fulgt opp - gir status done`() {
        val fulgtOpp = fulgtOppStatus()
        val blittFulgtOpp = fulgtOpp.blittFulgtOpp(twoDaysAgo, varselPublisher)

        verify { varselPublisher wasNot called }
        assertThat(blittFulgtOpp.status).isEqualTo(doneStatus)
        assertThat(blittFulgtOpp.statusTidspunkt).isEqualTo(twoDaysAgo)
    }






    @Test
    fun `bruker sett cv`() {
        val settCv = settCv()
        assertThat(settCv.status).isEqualTo(doneStatus)
    }

    @Test
    fun `bruker sett cv - varsle bruker - gir uendret status`() {
        val settCv = settCv()
        val forsoktVarslet = settCv.varsleBruker(varselPublisher)

        assertThat(forsoktVarslet.status).isEqualTo(settCv.status)
    }

    @Test
    fun `bruker sett cv - kommet under oppfolging - gir status skal varsles`() {
        val settCv = settCv()
        val underOppfolging = settCv.harKommetUnderOppfolging(twoDaysAgo)

        assertThat(underOppfolging.status).isEqualTo(skalVarslesStatus)
        assertThat(underOppfolging.statusTidspunkt).isEqualTo(twoDaysAgo)
    }

    @Test
    fun `bruker sett cv - sett cv - gir status uendret`() {
        val settCv = settCv()
        val settCvNy = settCv.harSettCv(twoDaysAgo, varselPublisher)

        assertThat(settCvNy.status).isEqualTo(doneStatus)
        assertThat(settCvNy.statusTidspunkt).isEqualTo(settCv.statusTidspunkt)
    }

    @Test
    fun `bruker sett cv - blitt fulgt opp - gir uendret status`() {
        val settCv = settCv()
        val blittFulgtOpp = settCv.blittFulgtOpp(twoDaysAgo, varselPublisher)

        assertThat(blittFulgtOpp.status).isEqualTo(settCv.status)
        assertThat(blittFulgtOpp.statusTidspunkt).isEqualTo(settCv.statusTidspunkt)
    }








    @Test
    fun `bruker under oppfolging`() {
        val kommetUnderOppfolging = kommetUnderOppfolging()
        assertThat(kommetUnderOppfolging.status).isEqualTo(skalVarslesStatus)
    }

    @Test
    fun `bruker under oppfolging - varsle bruker - varsel publiseres nar det finnes fodselsnummer`() {
        every { personIdentRepository.finnIdenter(aktor) } returns personIdenterAktorId

        val kommetUnderOppfolgingMedFnr = kommetUnderOppfolgingMedFnr()
        val forsoktVarsletSuksess = kommetUnderOppfolgingMedFnr.varsleBruker(varselPublisher)


        verify(exactly = 1) { varselPublisher.publish(kommetUnderOppfolgingMedFnr.uuid, aktorFnr) }
        assertThat(forsoktVarsletSuksess.status).isEqualTo(varsletStatus)
    }

    @Test
    fun `bruker under oppfolging - kommet under oppfolging - gir status uendret men oppdaterer dato med ny dato`() {
        val kommetUnderOppfolging = kommetUnderOppfolging()
        val underOppfolging = kommetUnderOppfolging.harKommetUnderOppfolging(yesterday)

        assertThat(underOppfolging.status).isEqualTo(skalVarslesStatus)
        assertThat(underOppfolging.statusTidspunkt).isEqualTo(yesterday)
    }

    @Test
    fun `bruker under oppfolging - sett cv - gir status done`() {
        val kommetUnderOppfolging = kommetUnderOppfolging()
        val settCv = kommetUnderOppfolging.harSettCv(twoDaysAgo, varselPublisher)

        verify { varselPublisher wasNot called }
        assertThat(settCv.status).isEqualTo(doneStatus)
        assertThat(settCv.statusTidspunkt).isEqualTo(twoDaysAgo)
    }

    @Test
    fun `bruker under oppfolging - blitt fulgt opp - gir status done`() {
        val kommetUnderOppfolging = kommetUnderOppfolging()
        val blittFulgtOpp = kommetUnderOppfolging.blittFulgtOpp(twoDaysAgo, varselPublisher)

        verify { varselPublisher wasNot called }
        assertThat(blittFulgtOpp.status).isEqualTo(doneStatus)
        assertThat(blittFulgtOpp.statusTidspunkt).isEqualTo(twoDaysAgo)
    }





    @Test
    fun `bruker varslet`() {
        val varslet = varsletStatus()
        assertThat(varslet.status).isEqualTo(varsletStatus)
    }


    @Test
    fun `bruker varslet - varsle bruker - gir uendret status`() {
        val varslet = varsletStatus()
        val nyVarslet = varslet.varsleBruker(varselPublisher)


        verify { listOf(varselPublisher) wasNot called }
        assertThat(nyVarslet.status).isEqualTo(varslet.status)
    }

    @Test
    fun `bruker varslet - kommet under oppfolging - gir uendret status`() {
        val varslet = varsletStatus()
        val underOppfolging = varslet.harKommetUnderOppfolging(yesterday)

        assertThat(underOppfolging.status).isEqualTo(varslet.status)
    }

    @Test
    fun `bruker varslet - sett cv - gir status done og done publiseres`() {
        val varslet = varsletStatus()
        val settCv = varslet.harSettCv(now, varselPublisher)

        verify(exactly = 1) { varselPublisher.done(varslet.uuid, aktorFnr) }
        assertThat(settCv.status).isEqualTo(doneStatus)
        assertThat(settCv.statusTidspunkt).isEqualTo(now)
    }

    @Test
    fun `bruker varslet - blitt fulgt opp - gir status done og done publiseres`() {
        val varslet = varsletStatus()
        val blittFulgtOpp = varslet.blittFulgtOpp(now, varselPublisher)

        verify(exactly = 1) { varselPublisher.done(varslet.uuid, aktorFnr) }
        assertThat(blittFulgtOpp.status).isEqualTo(doneStatus)
        assertThat(blittFulgtOpp.statusTidspunkt).isEqualTo(now)
    }






    private fun nyBruker() = Status.nyBruker("dummy")

    fun fulgtOppStatus() = nyBruker().blittFulgtOpp(twoDaysAgo, varselPublisher)

    fun settCv() = varsletStatus().harSettCv(twoDaysAgo, varselPublisher)

    fun kommetUnderOppfolging(): Status = nyBruker().harKommetUnderOppfolging(twoDaysAgo)

    fun kommetUnderOppfolgingMedFnr(): Status = nyBruker().harKommetUnderOppfolging(twoDaysAgo)
            .funnetFodselsnummer(aktorFnr)

    fun varsletStatus(): Status {
        val varselPublisher = mockk<VarselPublisher>(relaxed = true)
        val personIdentRepository = mockk <PersonIdentRepository>(relaxed = true)
        every { personIdentRepository.finnIdenter(aktor) } returns personIdenterAktorId
        return nyBruker().harKommetUnderOppfolging(twoDaysAgo)
                .funnetFodselsnummer(aktorFnr)
                .varsleBruker(varselPublisher)
    }

}