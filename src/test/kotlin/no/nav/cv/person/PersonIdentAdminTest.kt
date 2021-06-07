package no.nav.cv.person

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.slot
import io.mockk.verify
import no.nav.cv.notifikasjon.NotifikasjonAdmin
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@WebMvcTest(PersonIdentAdmin::class)
@TestPropertySource(properties = ["admin.enabled:enabled"])
class PersonIdentAdminTest {

    @Autowired
    private lateinit var mvc: MockMvc

    @MockkBean
    private lateinit var personIdentRepository: PersonIdentRepository

    @Test
    fun `that admin is enabled by property` () {

        every { personIdentRepository.oppdater(any()) } returns Unit

        mvc.perform(MockMvcRequestBuilders.get("/internal/ident/add/123/321"))
                .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `that correct idents are set` () {
       val slot = slot<PersonIdenter>()

        every { personIdentRepository.oppdater(capture(slot)) } returns Unit

        mvc.perform(MockMvcRequestBuilders.get("/internal/ident/add/222/333"))
                .andExpect(MockMvcResultMatchers.status().isOk)

        val identer = slot.captured.identer()
        assertAll(
                Executable { assertEquals(2, identer.size) },
                Executable { assertTrue(identer.containsAll(listOf(
                        PersonIdent("222", PersonIdent.Type.FOLKEREGISTER, true),
                        PersonIdent("333", PersonIdent.Type.AKTORID, true)
                ))) }
        )

    }

}

@WebMvcTest(PersonIdentAdmin::class)
@TestPropertySource(properties = ["admin.enabled:disabled"])
class PersonIdentAdminDisabledTest {

    @Autowired
    private lateinit var mvc: MockMvc

    @MockkBean
    private lateinit var personIdentRepository: PersonIdentRepository

    @Test
    fun `that admin is disabled by property` () {
        mvc.perform(MockMvcRequestBuilders.get("/internal/ident/add/123/321"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

}
