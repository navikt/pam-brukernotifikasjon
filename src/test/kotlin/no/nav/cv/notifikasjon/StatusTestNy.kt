package no.nav.cv.notifikasjon

import io.micronaut.test.annotation.MicronautTest
import io.micronaut.test.annotation.MockBean
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyAll
import no.nav.cv.person.PersonIdent
import no.nav.cv.person.PersonIdentRepository
import no.nav.cv.person.PersonIdenter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject

@MicronautTest(packages = ["no.nav.cv.notifikasjon"])
internal class StatusTestNy {
    val aktor = "dummy"
    val aktorFnr = "dummy_fnr"

    val personIdenterAktorId = PersonIdenter(listOf(
            PersonIdent(aktor, PersonIdent.Type.AKTORID, true),
            PersonIdent(aktorFnr, PersonIdent.Type.FOLKEREGISTER, true)
    ))

    @Inject
    lateinit var varselPublisher: VarselPublisher

    val personIdentRepository = mockk <PersonIdentRepository>(relaxed = true)

    @Inject
    lateinit var statusRepository: StatusRepository

    @Inject
    lateinit var hendelseService: HendelseService


    private val now = ZonedDateTime.now()
    private val yesterday = ZonedDateTime.now().minusDays(1)
    private val twoDaysAgo = ZonedDateTime.now().minusDays(2)
    private val agesAgo = ZonedDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"))

    @Test
    fun `ny bruker - riktig aktørId`() {
        val nyBruker = Status.nySession(aktor)

        assertEquals(aktor, nyBruker.aktorId)
    }

    @Test
    fun `ny bruker - sett cv - status cv oppdatert`() {
        val nyBruker = nyBruker()
        val cvOppdatert = nyBruker.harSettCv(now)

        assertEquals(cvOppdatertStatus, cvOppdatert.status)
        assertEquals(now, cvOppdatert.statusTidspunkt)
    }

    @Test
    fun `ny bruker - har kommet under oppfølging - status for gammel`() {
        val nyBruker = nyBruker()
        hendelseService.harKommetUnderOppfolging(nyBruker.aktorId, agesAgo)

        val lagretStatus = statusRepository.finnSiste(nyBruker.aktorId)

        assertEquals(forGammelStatus, lagretStatus.status)
        assertEquals(agesAgo, lagretStatus.statusTidspunkt)
    }

    @Test
    fun `ny bruker - har kommet under oppfølging - status skal varsles mangler fødselsnummer`() {
        val nyBruker = nyBruker()
        hendelseService.harKommetUnderOppfolging(nyBruker.aktorId, twoDaysAgo)

        val lagretStatus = statusRepository.finnSiste(nyBruker().aktorId)

        assertEquals(skalVarslesManglerFnrStatus, lagretStatus.status)
        assertEquals(twoDaysAgo, lagretStatus.statusTidspunkt)
    }

    @Test
    fun `bruker under oppfølging - fnr funnet - status skal varsles`() {
        val status = Status.skalVarlsesManglerFnr(Status.nySession("dummy2"), now)
        statusRepository.lagre(status)
        hendelseService.funnetFodselsnummer("dummy2", "dummy_fnr")

        val lagretStatus = statusRepository.finnSiste("dummy2")

        assertEquals(skalVarslesStatus, lagretStatus.status)
    }

    @Test
    fun `bruker under oppfølging – varsle bruker – status varslet`() {
        val status = Status.skalVarsles(statusRepository.finnSiste(aktor), aktorFnr)
        statusRepository.lagre(status)
        hendelseService.varsleBruker(aktor)

        val lagretStatus = statusRepository.finnSiste(aktor)

        verify { varselPublisher.publish(any(), aktorFnr) }
        assertEquals (varsletStatus, lagretStatus.status)
    }

    @Test
    fun `bruker varslet – ser cv – status sett cv`() {
        val status = Status.varslet(statusRepository.finnSiste(aktor), twoDaysAgo)
        statusRepository.lagre(status)
        hendelseService.harSettCv(aktor, now)

        val lagretStatus = statusRepository.finnSiste(aktor)

        verify { varselPublisher.done(any(), any()) }
        assertEquals (cvOppdatertStatus, lagretStatus.status)
    }

    @Test
    fun `bruker varslet – melding fra pto – status ikke under oppfølging`() {
        val status = Status.varslet(statusRepository.finnSiste(aktor), twoDaysAgo)
        statusRepository.lagre(status)
        hendelseService.ikkeUnderOppfolging(aktor, now)

        val lagretStatus = statusRepository.finnSiste(aktor)

        verify { varselPublisher.done(any(), any()) }
        assertEquals (ikkeUnderOppfølgingStatus, lagretStatus.status)
    }

    private fun nyBruker() = Status.nySession("dummy")

    @MockBean(VarselPublisher::class)
    fun varselPublisher() = mockk<VarselPublisher>(relaxed = true)
}