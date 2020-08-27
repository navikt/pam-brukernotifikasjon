package no.nav.cv.notifikasjon

import io.micronaut.test.annotation.MicronautTest
import io.micronaut.test.annotation.MockBean
import io.mockk.mockk
import io.mockk.verify
import no.nav.cv.person.PersonIdent
import no.nav.cv.person.PersonIdentRepository
import no.nav.cv.person.PersonIdenter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject

@MicronautTest(packages = ["no.nav.cv.notifikasjon"])
internal class StatusTestNy {
    val aktoer = "dummy"
    val aktoerFnr = "dummy_fnr"

    val personIdentRepository = mockk <PersonIdentRepository>(relaxed = true)

    val personIdenterAktorId = PersonIdenter(listOf(
            PersonIdent(aktoer, PersonIdent.Type.AKTORID, true),
            PersonIdent(aktoerFnr, PersonIdent.Type.FOLKEREGISTER, true)
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
    fun `ny bruker - riktig aktoerId`() {
        val nyBruker = Status.nySession(aktoer)

        assertEquals(aktoer, nyBruker.aktoerId)
    }

    @Test
    fun `ny bruker - har sett cv - status cvOppdatert`() {
        val nyBruker = nyBruker()
        hendelseService.harSettCv(nyBruker.aktoerId, now)

        val lagretStatus = statusRepository.finnSiste(nyBruker.aktoerId)

        assertEquals(cvOppdatertStatus, lagretStatus.status)
        assertEquals(now, lagretStatus.statusTidspunkt)
    }

    @Test
    fun `ny bruker - har kommet under oppfoelging - status forGammel`() {
        val nyBruker = nyBruker()
        hendelseService.harKommetUnderOppfolging(nyBruker.aktoerId, agesAgo)

        val lagretStatus = statusRepository.finnSiste(nyBruker.aktoerId)

        assertEquals(nyBrukerStatus, nyBruker.status)
        assertEquals(forGammelStatus, lagretStatus.status)
        assertEquals(agesAgo, lagretStatus.statusTidspunkt)
    }

    @Test
    fun `for gammel – har sett cv – status cvOppdatert`() {
        val status = Status.forGammel(forrigeStatus = statusRepository.finnSiste(aktoer), tidspunkt = agesAgo)
        statusRepository.lagre(status)
        hendelseService.harSettCv(aktoer, yesterday)

        val lagretStatus = statusRepository.finnSiste(aktoer)

        verify(exactly = 0) { varselPublisher.done(any(), any()) }
        assertEquals(cvOppdatertStatus, lagretStatus.status)
        assertEquals(yesterday, lagretStatus.statusTidspunkt)
    }

    @Test
    fun `for gammel – har kommet under oppfoelging – status skalVarslesManglerFoedselsnummer`() {
        val status = Status.forGammel(forrigeStatus = statusRepository.finnSiste(aktoer), tidspunkt = agesAgo)
        statusRepository.lagre(status)
        hendelseService.harKommetUnderOppfolging(aktoer, yesterday)

        val lagretStatus = statusRepository.finnSiste(aktoer)

        assertEquals(skalVarslesManglerFnrStatus, lagretStatus.status)
        assertEquals(yesterday, lagretStatus.statusTidspunkt)
    }

    @Test
    fun `ny bruker - har kommet under oppfoelging - status skalVarslesManglerFoedselsnummer`() {
        val nyBruker = nyBruker()
        hendelseService.harKommetUnderOppfolging(nyBruker.aktoerId, twoDaysAgo)

        val lagretStatus = statusRepository.finnSiste(nyBruker().aktoerId)

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
        val status = Status.skalVarsles(statusRepository.finnSiste(aktoer), aktoerFnr)
        statusRepository.lagre(status)
        hendelseService.varsleBruker(aktoer)

        val lagretStatus = statusRepository.finnSiste(aktoer)

        verify { varselPublisher.publish(any(), aktoerFnr) }
        assertEquals (varsletStatus, lagretStatus.status)
    }

    @Test
    fun `bruker varslet – har sett cv – status cvOppdatert`() {
        val status = Status.varslet(statusRepository.finnSiste(aktoer), twoDaysAgo)
        statusRepository.lagre(status)
        hendelseService.harSettCv(aktoer, now)

        val lagretStatus = statusRepository.finnSiste(aktoer)

        verify { varselPublisher.done(any(), any()) }
        assertEquals (cvOppdatertStatus, lagretStatus.status)
    }

    @Test
    fun `bruker varslet – melding fra pto – status ikkeUnderOppfoelging`() {
        val status = Status.varslet(statusRepository.finnSiste(aktoer), twoDaysAgo)
        statusRepository.lagre(status)
        hendelseService.ikkeUnderOppfolging(aktoer, now)

        val lagretStatus = statusRepository.finnSiste(aktoer)

        verify { varselPublisher.done(any(), any()) }
        assertEquals (ikkeUnderOppfølgingStatus, lagretStatus.status)
    }

    @Test
    fun `ikke under oppfoelging – har kommet under oppfoelging – status skalVarslesManglerFoedselsnummer`() {
        val status = Status.ikkeUnderOppfolging(statusRepository.finnSiste(aktoer), twoDaysAgo)
        val prevUUID = status.uuid
        statusRepository.lagre(status)
        hendelseService.harKommetUnderOppfolging(aktoer, yesterday)

        val lagretStatus = statusRepository.finnSiste(aktoer)

        assertNotEquals(prevUUID, lagretStatus.uuid)
        assertEquals(skalVarslesManglerFnrStatus, lagretStatus.status)
        assertEquals(yesterday, lagretStatus.statusTidspunkt)
    }

    @Test
    fun `ikke under oppfoelging – har sett cv – status cvOppdatert`() {
        val status = Status.ikkeUnderOppfolging(forrigeStatus = statusRepository.finnSiste(aktoer), tidspunkt = twoDaysAgo)
        statusRepository.lagre(status)
        hendelseService.harSettCv(aktoer, yesterday)

        val lagretStatus = statusRepository.finnSiste(aktoer)

        assertEquals(cvOppdatertStatus, lagretStatus.status)
        assertEquals(yesterday, lagretStatus.statusTidspunkt)
    }

    fun nyBruker() = Status.nySession("dummy")

    @MockBean(VarselPublisher::class)
    fun varselPublisher() = mockk<VarselPublisher>(relaxed = true)
}