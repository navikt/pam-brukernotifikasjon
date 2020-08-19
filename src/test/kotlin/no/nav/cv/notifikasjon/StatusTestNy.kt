package no.nav.cv.notifikasjon

import io.micronaut.test.annotation.MicronautTest
import io.micronaut.test.annotation.MockBean
import io.mockk.mockk
import no.nav.cv.person.PersonIdent
import no.nav.cv.person.PersonIdentRepository
import no.nav.cv.person.PersonIdenter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject

@MicronautTest
internal class StatusTestNy {
    val aktor = "dummy"
    val aktorFnr = "dummy_fnr"

    val personIdenterAktorId = PersonIdenter(listOf(
            PersonIdent(aktor, PersonIdent.Type.AKTORID, true),
            PersonIdent(aktorFnr, PersonIdent.Type.FOLKEREGISTER, true)
    ))


    val varselPublisher = mockk<VarselPublisher>(relaxed = true)

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



    private fun nyBruker() = Status.nySession("dummy")

    @MockBean(HendelseService::class)
    fun hendelseService(): HendelseService = mockk(relaxed = true)
}
