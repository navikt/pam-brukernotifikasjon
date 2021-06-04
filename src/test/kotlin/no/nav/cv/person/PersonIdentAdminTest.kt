package no.nav.cv.person

import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = ["admin.enabled:enabled"])
class PersonIdentAdminTest {

    @Autowired
    private lateinit var mvc: MockMvc

    @MockBean
    private lateinit var personIdentRepository: PersonIdentRepository

    @Test
    fun `that admin is enabled by property` () {

        mvc.perform(MockMvcRequestBuilders.get("/pam-brukernotifikasjon/internal/addIdent/123/321"))
                .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `that correct idents are set` () {
       val slot = slot<PersonIdenter>()

        mvc.perform(MockMvcRequestBuilders.get("/pam-brukernotifikasjon/internal/addIdent/222/333"))
                .andExpect(MockMvcResultMatchers.status().isOk)

        verify { personIdentRepository.oppdater(capture(slot)) }

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

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = ["admin.enabled:disabled"])
class PersonIdentAdminDisabledTest {

    @Autowired
    private lateinit var mvc: MockMvc

    @MockBean
    private lateinit var personIdentRepository: PersonIdentRepository

    @Test
    fun `that admin is disabled by property` () {
        mvc.perform(MockMvcRequestBuilders.get("/pam-brukernotifikasjon/internal/addIdent/123/321"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

}
