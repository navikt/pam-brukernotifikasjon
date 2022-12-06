package no.nav.cv.notifikasjon

import no.nav.cv.output.OutboxEntry
import no.nav.cv.output.OutboxRepository
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.time.ZonedDateTime
import java.util.*

private val uuid = UUID.randomUUID().toString()
private val fnr = "123dummy123"

@SpringBootTest
@ExtendWith(SpringExtension::class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = ["admin.enabled:enabled"])
@ContextConfiguration
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class NotifikasjonAdminTest {

    @Autowired
    lateinit var outboxRepository: OutboxRepository

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

}

@SpringBootTest
@ExtendWith(SpringExtension::class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = ["admin.enabled:disabled"])
@ContextConfiguration
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class NotifikasjonAdminDisabledTest {

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
