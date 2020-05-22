package no.nav.cv.notifikasjon

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import io.micronaut.test.annotation.MicronautTest
import io.micronaut.test.annotation.MockBean
import io.mockk.every
import io.mockk.mockk
import no.nav.cv.person.PersonIdent
import no.nav.cv.person.PersonIdentRepository
import no.nav.cv.person.PersonIdenter
import org.junit.jupiter.api.Test
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

        assertThat(status.uuid).isEqualTo(fetchedStatus.uuid)
    }

    @Test
    fun `that fetch latest fetches last`() {
        val statusOld = Status.nyBruker(aktorId)
        val statusNewer = Status.varslet(statusOld, "fnr", yesterday)
        val statusNewest = Status.done(statusNewer, now)
        statusRepository.lagre(statusOld)
        statusRepository.lagre(statusNewer)
        statusRepository.lagre(statusNewest)

        val fetchedStatus = statusRepository.finnSiste(aktorId)

        assertThat(statusNewest.uuid).isEqualTo(fetchedStatus.uuid)
        assertThat(statusNewest.status).isEqualTo(fetchedStatus.status)
        assertThat(statusNewest.statusTidspunkt).isEqualTo(fetchedStatus.statusTidspunkt)
    }

    @Test
    fun `that latest fetches correct foedselsnummer`() {
        val savedFirst = Status.nyBruker(aktorId)
        val savedLast = Status.nyBruker(aktorId2)

        statusRepository.lagre(savedFirst)
        statusRepository.lagre(savedLast)

        val fetchedStatus = statusRepository.finnSiste(aktorId)

        assertThat(savedFirst.uuid).isEqualTo(fetchedStatus.uuid)
    }


    @Test
    fun `at hentSkalVarsles returnerer alle som skal varsles`() {
        val new1 = Status.nyBruker(aktorId)
        val underOppfolging1 = new1.harKommetUnderOppfolging(now)
        val new2 = Status.nyBruker(aktorId2)
        val underOppfolging2 = new2.harKommetUnderOppfolging(now)

        statusRepository.lagre(new1)
        statusRepository.lagre(new2)
        statusRepository.lagre(underOppfolging1)
        statusRepository.lagre(underOppfolging2)

        val skalVarslesListe = statusRepository.skalVarsles()

        assertThat(skalVarslesListe).hasSize(2)
        assertThat(skalVarslesListe.map {it.aktorId} ).containsExactly(aktorId, aktorId2)
    }


    @Test
    fun `skal kun hente siste varsel`() {
        every { personIdentRepositoryMock.finnIdenter(aktorId) } returns personIdenterAktorId

        val new1 = Status.nyBruker(aktorId)
        val underOppfolging1 = new1.harKommetUnderOppfolging(yesterday)
        val erVarslet = underOppfolging1.varsleBruker(
                yesterday.plusSeconds(30),
                personIdentRepositoryMock,
                varselPublisherMock
        )
        val settCv = erVarslet.harSettCv(yesterday.plusMinutes(1), varselPublisherMock)
        val nyOppfolging = settCv.harKommetUnderOppfolging(now)

        statusRepository.lagre(new1)
        statusRepository.lagre(underOppfolging1)
        statusRepository.lagre(settCv)
        statusRepository.lagre(nyOppfolging)


        val skalVarslesListe = statusRepository.skalVarsles()

        assertThat(skalVarslesListe).hasSize(1)
    }

    @MockBean(VarselPublisher::class)
    fun varselPublisher(): VarselPublisher = mockk(relaxed = true)

    @MockBean(PersonIdentRepository::class)
    fun personIdentRepository(): PersonIdentRepository = mockk(relaxed = true)

}