package no.nav.cv.notifikasjon

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nav.cv.personident.PersonOppslag
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
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
        statusRepository.lagreNyStatus(status)
        val fetchedStatus = statusRepository.finnSiste(aktorId)

        assertEquals(status.uuid, fetchedStatus.uuid)
    }

    @Test
    fun `that fetch latest fetches last`() {
        val statusOld = Status.nySession(aktorId)
        statusRepository.lagreNyStatus(statusOld)

        val statusNewer = statusOld.varslet("fnr", yesterday)
        statusRepository.lagreNyStatus(statusNewer)

        val statusNewest = statusNewer.endretCV(now)
        statusRepository.lagreNyStatus(statusNewest)

        val fetchedStatus = statusRepository.finnSiste(aktorId)

        assertEquals(statusNewest.uuid, fetchedStatus.uuid)
        assertEquals(statusNewest.aktoerId, fetchedStatus.aktoerId)
        assertEquals(statusNewest.fnr, fetchedStatus.fnr)
        assertEquals(statusNewest.status, fetchedStatus.status)
        assertEquals(statusNewest.statusTidspunkt, fetchedStatus.statusTidspunkt)
    }

    @Test
    fun `that latest fetches correct foedselsnummer`() {
        val savedFirst = Status.nySession(aktorId)
        val savedLast = Status.nySession(aktorId2)

        statusRepository.lagreNyStatus(savedFirst)
        statusRepository.lagreNyStatus(savedLast)

        val fetchedStatus = statusRepository.finnSiste(aktorId)

        assertEquals(savedFirst.uuid, fetchedStatus.uuid)
    }


}
