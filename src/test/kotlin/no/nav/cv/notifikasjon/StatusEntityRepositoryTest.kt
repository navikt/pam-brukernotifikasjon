package no.nav.cv.notifikasjon

import io.micronaut.test.annotation.MicronautTest
import io.micronaut.test.annotation.MockBean
import io.mockk.every
import io.mockk.mockk
import no.nav.cv.person.PersonIdent
import no.nav.cv.person.PersonIdentRepository
import no.nav.cv.person.PersonIdenter
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import java.time.ZonedDateTime
import javax.inject.Inject

@MicronautTest
class StatusEntityRepositoryTest {

    val aktorId = "dummy"
    val aktorId2 = "dummy2"

    val personIdenterAktorId = PersonIdenter(listOf(
            PersonIdent("dummy", PersonIdent.Type.AKTORID, true),
            PersonIdent("dummy_fnr", PersonIdent.Type.FOLKEREGISTER, true)
    ))

    @Inject
    lateinit var statusRepository: StatusRepository

    @Inject
    lateinit var personIdentRepositoryMock: PersonIdentRepository

    @Inject
    lateinit var varselPublisherMock: VarselPublisher

    private val now = ZonedDateTime.now()
    private val yesterday = ZonedDateTime.now().minusDays(1)
    private val twoDaysAgo = ZonedDateTime.now().minusDays(2)

    @Test
    fun `save and fetch`() {
        val status = Status.nyBruker(aktorId)
        statusRepository.lagre(status)
        val fetchedStatus = statusRepository.finnSiste(aktorId)

        assertEquals(status.uuid, fetchedStatus.uuid)
    }

    @Test
    fun `that fetch latest fetches last`() {
        val statusOld = Status.nyBruker(aktorId)
        val statusNewer = Status.varslet(statusOld, yesterday)
        val statusNewest = Status.done(statusNewer, now)
        statusRepository.lagre(statusOld)
        statusRepository.lagre(statusNewer)
        statusRepository.lagre(statusNewest)

        val fetchedStatus = statusRepository.finnSiste(aktorId)

        assertEquals(statusNewest.uuid, fetchedStatus.uuid)
        assertEquals(statusNewest.status, fetchedStatus.status)
        assertEquals(statusNewest.statusTidspunkt, fetchedStatus.statusTidspunkt)
    }

    @Test
    fun `that latest fetches correct foedselsnummer`() {
        val savedFirst = Status.nyBruker(aktorId)
        val savedLast = Status.nyBruker(aktorId2)

        statusRepository.lagre(savedFirst)
        statusRepository.lagre(savedLast)

        val fetchedStatus = statusRepository.finnSiste(aktorId)

        assertEquals(savedFirst.uuid, fetchedStatus.uuid)
    }


    @Test
    fun `at hentSkalVarsles returnerer alle som skal varsles`() {
        val new1 = Status.nyBruker(aktorId)
        val underOppfolging1 = new1.harKommetUnderOppfolging(now, ABTest.skalVarsles)
        val medFnr1 = underOppfolging1.funnetFodselsnummer("4321")
        val new2 = Status.nyBruker(aktorId2)
        val underOppfolging2 = new2.harKommetUnderOppfolging(now, ABTest.skalVarsles)
        val medFnr2 = underOppfolging2.funnetFodselsnummer("1234")

        statusRepository.lagre(new1)
        statusRepository.lagre(new2)
        statusRepository.lagre(underOppfolging1)
        statusRepository.lagre(underOppfolging2)
        statusRepository.lagre(medFnr1)
        statusRepository.lagre(medFnr2)

        val skalVarslesListe = statusRepository.skalVarsles()

        assertAll(
                Executable { assertEquals(skalVarslesListe.size, 2)  },
                Executable { assertTrue(skalVarslesListe.map {it.aktorId}.contains(aktorId)) },
                Executable { assertTrue(skalVarslesListe.map {it.aktorId}.contains(aktorId2)) }
        )

    }


    @Test
    fun `skal kun hente siste varsel`() {
        every { personIdentRepositoryMock.finnIdenter(aktorId) } returns personIdenterAktorId

        val new1 = Status.nyBruker(aktorId)
        val underOppfolging1 = new1.harKommetUnderOppfolging(yesterday, ABTest.skalVarsles)
        val medFnr = underOppfolging1.funnetFodselsnummer("anything")
        val erVarslet = medFnr.varsleBruker(
                varselPublisherMock
        )
        val settCv = erVarslet.harSettCv(ZonedDateTime.now(), varselPublisherMock)

        val nyOppfolging = settCv.harKommetUnderOppfolging(ZonedDateTime.now(), ABTest.skalVarsles)
        val nyOppfolgingMedFnr = nyOppfolging.funnetFodselsnummer("anything")

        statusRepository.lagre(new1)
        statusRepository.lagre(underOppfolging1)
        statusRepository.lagre(medFnr)
        statusRepository.lagre(erVarslet)
        statusRepository.lagre(settCv)

        statusRepository.lagre(nyOppfolging)
        statusRepository.lagre(nyOppfolgingMedFnr)


        val skalVarslesListe = statusRepository.skalVarsles()

        assertEquals(skalVarslesListe.size, 1)
    }

    @MockBean(VarselPublisher::class)
    fun varselPublisher(): VarselPublisher = mockk(relaxed = true)

    @MockBean(PersonIdentRepository::class)
    fun personIdentRepository(): PersonIdentRepository = mockk(relaxed = true)

}