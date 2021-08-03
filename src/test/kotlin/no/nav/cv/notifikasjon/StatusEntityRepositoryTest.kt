package no.nav.cv.notifikasjon

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nav.cv.personident.PersonOppslag
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.function.Executable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.stereotype.Repository
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.ZonedDateTime

@DataJpaTest(includeFilters = [ComponentScan.Filter(type = FilterType.ANNOTATION, classes = [Repository::class])])
@ExtendWith(SpringExtension::class)
class StatusEntityRepositoryTest {

    val aktorId = "dummy"
    val aktorId2 = "dummy2"


    @MockkBean(PersonOppslag::class)
    private lateinit var personOppslag : PersonOppslag

    @BeforeEach
    fun setupPersonoppslag() {
        every { personOppslag.hentAktorId(aktorId) } returns "dummy_fnr"
    }

    @Autowired
    lateinit var statusRepository: StatusRepository

    @MockkBean(relaxed = true)
    lateinit var varselPublisherMock: VarselPublisher

    private val now = ZonedDateTime.now()
    private val yesterday = ZonedDateTime.now().minusDays(1)
    private val twoDaysAgo = ZonedDateTime.now().minusDays(2)

    @Test
    fun `save and fetch`() {
        val status = Status.nySession(aktorId)
        statusRepository.lagre(status)
        val fetchedStatus = statusRepository.finnSiste(aktorId)

        assertEquals(status.uuid, fetchedStatus.uuid)
    }

    @Test
    fun `that fetch latest fetches last`() {
        val statusOld = Status.nySession(aktorId)
        val statusNewer = Status.varslet(statusOld, yesterday)
        val statusNewest = Status.endretCV(statusNewer, now)
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
        val savedFirst = Status.nySession(aktorId)
        val savedLast = Status.nySession(aktorId2)

        statusRepository.lagre(savedFirst)
        statusRepository.lagre(savedLast)

        val fetchedStatus = statusRepository.finnSiste(aktorId)

        assertEquals(savedFirst.uuid, fetchedStatus.uuid)
    }


    @Test
    fun `at hentSkalVarsles returnerer alle som skal varsles`() {
        val new1 = Status.nySession(aktorId)
        val underOppfolging1 = new1.skalVarlsesManglerFnr(now)
        val medFnr1 = underOppfolging1.skalVarsles("4321")
        val new2 = Status.nySession(aktorId2)
        val underOppfolging2 = new2.skalVarlsesManglerFnr(now)
        val medFnr2 = underOppfolging2.skalVarsles("1234")

        statusRepository.lagre(new1)
        statusRepository.lagre(new2)
        statusRepository.lagre(underOppfolging1)
        statusRepository.lagre(underOppfolging2)
        statusRepository.lagre(medFnr1)
        statusRepository.lagre(medFnr2)

        val skalVarslesListe = statusRepository.skalVarsles()

        assertAll(
                Executable { assertEquals(skalVarslesListe.size, 2)  },
                Executable { assertTrue(skalVarslesListe.map {it.aktoerId}.contains(aktorId)) },
                Executable { assertTrue(skalVarslesListe.map {it.aktoerId}.contains(aktorId2)) }
        )

    }



}
