package no.nav.cv.notifikasjon

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.micronaut.test.annotation.MicronautTest
import no.nav.cv.notifikasjon.JpaStatusRepository
import no.nav.cv.notifikasjon.Status
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime
import javax.inject.Inject

@MicronautTest
class JpaStatusEntityRepositoryTest {

    val fnr = "dummy"
    val fnr2 = "dummy2"

    @Inject
    lateinit var statusRepository: JpaStatusRepository

    private val now = ZonedDateTime.now()
    private val yesterday = ZonedDateTime.now().minusDays(1)
    private val twoDaysAgo = ZonedDateTime.now().minusDays(2)

    @Test
    fun `save and fetch`() {
        val status = Status.ukjent(fnr)
        statusRepository.lagre(status)
        val fetchedStatus = statusRepository.finnSiste(fnr)

        assertThat(status.uuid).isEqualTo(fetchedStatus.uuid)
    }

    @Test
    fun `that fetch latest fetches last`() {
        val statusOld = Status.ukjent(fnr)
        val statusNewer = Status.varslet(statusOld.uuid, fnr, yesterday)
        val statusNewest = Status.done(statusOld.uuid, fnr, now)
        statusRepository.lagre(statusOld)
        statusRepository.lagre(statusNewer)
        statusRepository.lagre(statusNewest)

        val fetchedStatus = statusRepository.finnSiste(fnr)

        assertThat(statusNewest.uuid).isEqualTo(fetchedStatus.uuid)
        assertThat(statusNewest.status).isEqualTo(fetchedStatus.status)
        assertThat(statusNewest.statusTidspunkt).isEqualTo(fetchedStatus.statusTidspunkt)
    }

    @Test
    fun `that latest fetches correct foedselsnummer`() {
        val savedFirst = Status.ukjent(fnr)
        val savedLast = Status.ukjent(fnr2)

        statusRepository.lagre(savedFirst)
        statusRepository.lagre(savedLast)

        val fetchedStatus = statusRepository.finnSiste(fnr)

        assertThat(savedFirst.uuid).isEqualTo(fetchedStatus.uuid)
    }

}