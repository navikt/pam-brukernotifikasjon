package no.nav.cv.notifikasjon

import no.nav.cv.SingletonPostgresTestInstance
import no.nav.cv.output.OutboxEntry
import no.nav.cv.output.OutboxRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

private val uuid = UUID.randomUUID().toString()
private val fnr = "123dummy123"

@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = ["admin.enabled=true"])
class NotifikasjonAdminTestInstance : SingletonPostgresTestInstance() {

    @Autowired
    lateinit var outboxRepository: OutboxRepository

    @Autowired
    lateinit var statusRepository: StatusRepository

    @Autowired
    private lateinit var mvc: MockMvc

    @Test
    fun `varsel blir køet`() {

        mvc.perform(MockMvcRequestBuilders.get("/internal/kafka/manuell/varsel/${uuid}/${fnr}"))
            .andExpect(MockMvcResultMatchers.status().isOk)

        val allEntries = outboxRepository.findAllUnprocessedMessages(ZonedDateTime.now().plusDays(1))

        assertTrue {
            allEntries.any {
                it.type == OutboxEntry.OutboxEntryType.VARSLE
                        && it.uuid == uuid
                        && it.foedselsnummer == fnr
            }
        }
    }

    @Test
    fun `done blir køet`() {
        mvc.perform(MockMvcRequestBuilders.get("/internal/kafka/manuell/donemelding/${uuid}/${fnr}"))
            .andExpect(MockMvcResultMatchers.status().isOk)

        val allEntries = outboxRepository.findAllUnprocessedMessages(ZonedDateTime.now().plusDays(1))

        assertTrue {
            allEntries.any {
                it.type == OutboxEntry.OutboxEntryType.DONE
                        && it.uuid == uuid
                        && it.foedselsnummer == fnr
            }
        }
    }

    @Test
    fun `Ved lukking av alle blir done-melding schedulert og ferdig satt til true for aktuelle statuser`() {
        val uuid = UUID.randomUUID()
        val aktørId = "1".repeat(13)
        val fnr = "1".repeat(11)
        val tidspunkt = ZonedDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneId.of("Europe/Oslo"))

        // To første settes til ferdig ved kall til lagreNyStatus
        statusRepository.lagreNyStatus(Status(UUID.randomUUID(), aktørId, ukjentFnr, nyBrukerStatus, tidspunkt.minusDays(2)))
        statusRepository.lagreNyStatus(Status(UUID.randomUUID(), aktørId, ukjentFnr, skalVarslesManglerFnrStatus, tidspunkt.minusDays(1)))
        statusRepository.lagreNyStatus(Status(uuid, aktørId, fnr, varsletStatus, tidspunkt))

        // Skal ignoreres
        statusRepository.lagreNyStatus(Status(UUID.randomUUID(), "2".repeat(13), "2".repeat(11), "cvEndret", tidspunkt))

        var alleForAktørId = statusRepository.finnAlleForAktørId(aktørId)
        var siste = statusRepository.finnAlleForAktørId(aktørId).sortedByDescending { it.statusTidspunkt }.first()

        assertEquals(3, alleForAktørId.size)
        assertEquals(uuid, siste.uuid)
        assertEquals(varsletStatus, siste.status)
        zonedDateTimeEquals(tidspunkt.withFixedOffsetZone(), siste.statusTidspunkt.withFixedOffsetZone())
        assertEquals(1, statusRepository.finnAlleMedÅpneVarsler().size)

        mvc.perform(MockMvcRequestBuilders.get("/internal/kafka/manuell/lukk-alle")).andExpect(MockMvcResultMatchers.status().isOk)

        val allEntries = outboxRepository.findAllUnprocessedMessages(ZonedDateTime.now().plusDays(1))

        alleForAktørId = statusRepository.finnAlleForAktørId(aktørId)
        siste = statusRepository.finnAlleForAktørId(aktørId).sortedByDescending { it.statusTidspunkt }.first()

        assertEquals(3, alleForAktørId.size)
        assertEquals(uuid, siste.uuid)
        assertEquals(varsletStatus, siste.status)
        assertEquals(0, statusRepository.finnAlleMedÅpneVarsler().size)
        assertEquals(1, allEntries.size)

        assertEquals(OutboxEntry.OutboxEntryType.DONE, allEntries[0].type)
        assertEquals(uuid.toString(), allEntries[0].uuid)
        assertEquals(fnr, allEntries[0].foedselsnummer)
    }

    // Brukes pga tidssone og presisjonsforskjeller med databasen i GHA
    private fun zonedDateTimeEquals(expected: ZonedDateTime, actual: ZonedDateTime?) {
        assertEquals(expected.withNano(0).toInstant(), actual?.withNano(0)?.toInstant())
    }
}

@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = ["admin.enabled=false"])
class NotifikasjonAdminDisabledTestInstance : SingletonPostgresTestInstance() {

    @Autowired
    private lateinit var mvc: MockMvc

    @Test
    fun `that admin is disabled by property for done messages`() {

        mvc.perform(MockMvcRequestBuilders.get("/internal/kafka/manuell/donemelding/${uuid}/${fnr}"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    @Test
    fun `that admin is disabled by property for publish messages`() {

        mvc.perform(MockMvcRequestBuilders.get("/internal/kafka/manuell/varsel/${uuid}/${fnr}"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

}
