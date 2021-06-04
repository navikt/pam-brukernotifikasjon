package no.nav.cv.notifikasjon

import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.util.*

private val uuid = UUID.randomUUID()
private val fnr = "123dummy123"

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = ["admin.enabled:enabled"])
class NotifikasjonAdminTest {

    @Autowired
    private lateinit var mvc: MockMvc

    @MockBean
    private lateinit var varselPublisher : VarselPublisher

    @Test
    fun `varsel blir sendt` () {
        val uuidSlot = slot<UUID>()
        val fnrSlot = slot<String>()
        mvc.perform(MockMvcRequestBuilders.get("/pam-brukernotifikasjon/internal/kafka/manuell/varsel/${uuid}/${fnr}"))
                .andExpect(MockMvcResultMatchers.status().isOk)

        verify { varselPublisher.publish(capture(uuidSlot), capture(fnrSlot)) }

        assertEquals(uuidSlot.captured, uuid)
        assertEquals(fnrSlot.captured, fnr)

    }

    @Test
    fun `done blir sendt` () {
        val uuidSlot = slot<UUID>()
        val fnrSlot = slot<String>()

        mvc.perform(MockMvcRequestBuilders.get("/pam-brukernotifikasjon/internal/kafka/manuell/donemelding/${uuid}/${fnr}"))
                .andExpect(MockMvcResultMatchers.status().isOk)

        verify { varselPublisher.done(capture(uuidSlot), capture(fnrSlot)) }

        assertEquals(uuidSlot.captured, uuid)
        assertEquals(fnrSlot.captured, fnr)

    }

}


@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = ["admin.enabled:disabled"])
class NotifikasjonAdminDisabledTest {

    @Autowired
    private lateinit var mvc: MockMvc

    @Test
    fun `that admin is disabled by property for done messages` () {

        mvc.perform(MockMvcRequestBuilders.get("/pam-brukernotifikasjon/internal/kafka/manuell/donemelding/${uuid}/${fnr}"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    @Test
    fun `that admin is disabled by property for publish messages` () {

        mvc.perform(MockMvcRequestBuilders.get("/pam-brukernotifikasjon/internal/kafka/manuell/varsel/${uuid}/${fnr}"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

}
