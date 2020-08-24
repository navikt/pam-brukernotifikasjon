package no.nav.cv.notifikasjon

import io.micronaut.test.annotation.MicronautTest
import io.micronaut.test.annotation.MockBean
import io.mockk.mockk
import io.mockk.verify
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

    val personIdentRepository = mockk <PersonIdentRepository>(relaxed = true)

    val personIdenterAktorId = PersonIdenter(listOf(
            PersonIdent(aktor, PersonIdent.Type.AKTORID, true),
            PersonIdent(aktorFnr, PersonIdent.Type.FOLKEREGISTER, true)
    ))

    private val now = ZonedDateTime.now()
    private val yesterday = ZonedDateTime.now().minusDays(1)
    private val twoDaysAgo = ZonedDateTime.now().minusDays(2)
    private val agesAgo = ZonedDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"))

    @Inject
    lateinit var varselPublisher: VarselPublisher

    @Inject
    lateinit var statusRepository: StatusRepository

    @Inject
    lateinit var hendelseService: HendelseService


    @Test
    fun `ny bruker - riktig aktørId`() {
        val nyBruker = Status.nySession(aktor)

        assertEquals(nyBrukerStatus, nyBruker.status)
        assertEquals(aktor, nyBruker.aktorId)
    }

    @Test
    fun `ny bruker - har sett cv - status cvOppdatert`() {
        val nyBruker = nyBruker()
        val cvOppdatert = nyBruker.harSettCv(now)

        assertEquals(nyBrukerStatus, nyBruker.status)
        assertEquals(cvOppdatertStatus, cvOppdatert.status)
        assertEquals(now, cvOppdatert.statusTidspunkt)
    }

    @Test
    fun `ny bruker - har kommet under oppfoelging - status forGammel`() {
        val nyBruker = nyBruker()
        hendelseService.harKommetUnderOppfolging(nyBruker.aktorId, agesAgo)

        val lagretStatus = statusRepository.finnSiste(nyBruker.aktorId)

        assertEquals(nyBrukerStatus, nyBruker.status)
        assertEquals(forGammelStatus, lagretStatus.status)
        assertEquals(agesAgo, lagretStatus.statusTidspunkt)
    }

    @Test
    fun `for gammel – har sett cv – status cvOppdatert`() {
        val status = Status.forGammel(forrigeStatus = statusRepository.finnSiste(aktor), tidspunkt = agesAgo)
        statusRepository.lagre(status)
        hendelseService.harSettCv(aktor, yesterday)

        val lagretStatus = statusRepository.finnSiste(aktor)

        verify(exactly = 0) { varselPublisher.done(any(), any()) }
        assertEquals(cvOppdatertStatus, lagretStatus.status)
        assertEquals(yesterday, lagretStatus.statusTidspunkt)
    }

    @Test
    fun `for gammel – har kommet under oppfoelging – status skalVarslesManglerFoedselsnummer`() {
        val status = Status.forGammel(forrigeStatus = statusRepository.finnSiste(aktor), tidspunkt = agesAgo)
        statusRepository.lagre(status)
        hendelseService.harKommetUnderOppfolging(aktor, yesterday)

        val lagretStatus = statusRepository.finnSiste(aktor)

        assertEquals(skalVarslesManglerFnrStatus, lagretStatus.status)
        assertEquals(yesterday, lagretStatus.statusTidspunkt)
    }

    @Test
    fun `ny bruker - har kommet under oppfoelging - status skalVarslesManglerFoedselsnummer`() {
        val nyBruker = nyBruker()
        hendelseService.harKommetUnderOppfolging(nyBruker.aktorId, twoDaysAgo)

        val lagretStatus = statusRepository.finnSiste(nyBruker().aktorId)

        assertEquals(nyBrukerStatus, nyBruker.status)
        assertEquals(skalVarslesManglerFnrStatus, lagretStatus.status)
        assertEquals(twoDaysAgo, lagretStatus.statusTidspunkt)
    }

    @Test
    fun `bruker under oppfoelging - fnr funnet - status skalVarsles`() {
        val status = Status.skalVarlsesManglerFnr(Status.nySession("dummy2"), now)
        statusRepository.lagre(status)
        hendelseService.funnetFodselsnummer("dummy2", "dummy_fnr")

        val lagretStatus = statusRepository.finnSiste("dummy2")

        assertEquals(skalVarslesStatus, lagretStatus.status)
    }

    @Test
    fun `bruker under oppfoelging – varsle bruker – status varslet`() {
        val status = Status.skalVarsles(statusRepository.finnSiste(aktor), aktorFnr)
        statusRepository.lagre(status)
        hendelseService.varsleBruker(aktor)

        val lagretStatus = statusRepository.finnSiste(aktor)

        verify { varselPublisher.publish(any(), aktorFnr) }
        assertEquals (varsletStatus, lagretStatus.status)
    }

    @Test
    fun `bruker varslet – har sett cv – status cvOppdatert`() {
        val status = Status.varslet(statusRepository.finnSiste(aktor), twoDaysAgo)
        statusRepository.lagre(status)
        hendelseService.harSettCv(aktor, now)

        val lagretStatus = statusRepository.finnSiste(aktor)

        verify { varselPublisher.done(any(), any()) }
        assertEquals (cvOppdatertStatus, lagretStatus.status)
    }

    @Test
    fun `bruker varslet – melding fra pto – status ikkeUnderOppfoelging`() {
        val status = Status.varslet(statusRepository.finnSiste(aktor), twoDaysAgo)
        statusRepository.lagre(status)
        hendelseService.ikkeUnderOppfolging(aktor, now)

        val lagretStatus = statusRepository.finnSiste(aktor)

        verify { varselPublisher.done(any(), any()) }
        assertEquals (ikkeUnderOppfølgingStatus, lagretStatus.status)
    }

    @Test
    fun `ikke under oppfoelging – har kommet under oppfoelging – status skalVarslesManglerFoedselsnummer`() {
        val status = Status.ikkeUnderOppfolging(forrigeStatus = statusRepository.finnSiste(aktor), tidspunkt = twoDaysAgo)
        statusRepository.lagre(status)
        hendelseService.harKommetUnderOppfolging(aktor, yesterday)

        val lagretStatus = statusRepository.finnSiste(aktor)

        assertEquals(skalVarslesManglerFnrStatus, lagretStatus.status)
        assertEquals(yesterday, lagretStatus.statusTidspunkt)
    }

    @Test
    fun `ikke under oppfoelging – har sett cv – status cvOppdatert`() {
        val status = Status.ikkeUnderOppfolging(forrigeStatus = statusRepository.finnSiste(aktor), tidspunkt = twoDaysAgo)
        statusRepository.lagre(status)
        hendelseService.harSettCv(aktor, yesterday)

        val lagretStatus = statusRepository.finnSiste(aktor)

        assertEquals(cvOppdatertStatus, lagretStatus.status)
        assertEquals(yesterday, lagretStatus.statusTidspunkt)
    }

    fun nyBruker() = Status.nySession("dummy")

    @MockBean(VarselPublisher::class)
    fun varselPublisher() = mockk<VarselPublisher>(relaxed = true)
}