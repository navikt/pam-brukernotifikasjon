package no.nav.cv.notifikasjon

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.slot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.util.*

private val uuid = UUID.randomUUID()
private val fnr = "123dummy123"

@ExtendWith(SpringExtension::class)
@WebMvcTest(NotifikasjonAdmin::class)
@AutoConfigureMockMvc(addFilters=false)
@TestPropertySource(properties = ["admin.enabled:enabled"])
@ContextConfiguration
class NotifikasjonAdminTest {

    @Autowired
    private lateinit var mvc: MockMvc

    @MockkBean
    private lateinit var varselPublisher : VarselPublisher

    @Test
    fun `varsel blir sendt` () {

        val uuidSlot = slot<UUID>()
        val fnrSlot = slot<String>()

        every { varselPublisher.publish(capture(uuidSlot), capture(fnrSlot)) } returns Unit

        mvc.perform(MockMvcRequestBuilders.get("/internal/kafka/manuell/varsel/${uuid}/${fnr}"))
                .andExpect(MockMvcResultMatchers.status().isOk)


        assertEquals(uuidSlot.captured, uuid)
        assertEquals(fnrSlot.captured, fnr)

    }

    @Test
    fun `done blir sendt` () {
        val uuidSlot = slot<UUID>()
        val fnrSlot = slot<String>()

        every { varselPublisher.done(capture(uuidSlot), capture(fnrSlot)) } returns Unit

        mvc.perform(MockMvcRequestBuilders.get("/internal/kafka/manuell/donemelding/${uuid}/${fnr}"))
                .andExpect(MockMvcResultMatchers.status().isOk)

        assertEquals(uuidSlot.captured, uuid)
        assertEquals(fnrSlot.captured, fnr)

    }

}

@WebMvcTest(NotifikasjonAdmin::class)
@TestPropertySource(properties = ["admin.enabled:disabled"])
class NotifikasjonAdminDisabledTest {

    @Autowired
    private lateinit var mvc: MockMvc

    @MockkBean
    private lateinit var varselPublisher : VarselPublisher

    @Test
    fun `that admin is disabled by property for done messages` () {

        mvc.perform(MockMvcRequestBuilders.get("/internal/kafka/manuell/donemelding/${uuid}/${fnr}"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    @Test
    fun `that admin is disabled by property for publish messages` () {

        mvc.perform(MockMvcRequestBuilders.get("/internal/kafka/manuell/varsel/${uuid}/${fnr}"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

}
