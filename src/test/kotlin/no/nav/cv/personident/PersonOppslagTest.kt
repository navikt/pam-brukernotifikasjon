package no.nav.cv.personident

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.Header
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse
import java.util.function.Supplier

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PersonOppslagServiceTest {

    private lateinit var mockServer: ClientAndServer

    @BeforeEach
    fun startMockServer() {
        mockServer = ClientAndServer.startClientAndServer(45678);
    }

    @Test
    fun `slaa opp aktorid som finnes`() {
        val aktorId = "2000003739123"
        val fnr = "11223312345"
        mockServer.`when`(
            request().withPath("/pam-personoppslag/personidenter/system/oppslag/$fnr")
        ).respond(HttpResponse.response()
            .withHeaders(Header("Content-Type", "application/json"))
            .withBody(responseJson(fnr, aktorId))
        )

        val personOppslag =   PersonOppslag("http://localhost:${mockServer.port}", Supplier { "" })

        val response = personOppslag.hentAktorId(fnr)

        assertEquals(aktorId, response)

    }

    @Test
    fun `slaa opp fodselsnummer som finnes`() {
        val aktorId = "2000003739123"
        val fnr = "11223312345"
        mockServer.`when`(
            request().withPath("/pam-personoppslag/personidenter/system/oppslag/$aktorId")
        ).respond(HttpResponse.response()
            .withHeaders(Header("Content-Type", "application/json"))
            .withBody(responseJson(fnr, aktorId))
        )

        val personOppslag =   PersonOppslag("http://localhost:${mockServer.port}", Supplier { "" })

        val response = personOppslag.hentFoedselsnummer(aktorId)

        assertEquals(fnr, response)

    }

    @Test
    fun  `slaa opp aktorid som ikke finnes`() {
        val aktorId = null
        val fnr = "11223312345"
        mockServer.`when`(
            request().withPath("/pam-personoppslag/personidenter/system/oppslag/$fnr")
        ).respond(HttpResponse.response()
            .withHeaders(Header("Content-Type", "application/json"))
            .withBody(responseJson(fnr, aktorId))
        )

        val personOppslag =   PersonOppslag("http://localhost:${mockServer.port}", Supplier { "" })

        assertNull(personOppslag.hentAktorId(fnr))

    }

    @Test
    fun `slaa opp fodselsnummer som ikke finnes`() {
        val aktorId = "2000003739123"
        val fnr = null
        mockServer.`when`(
            request().withPath("/pam-personoppslag/personidenter/system/oppslag/$aktorId")
        ).respond(HttpResponse.response()
            .withHeaders(Header("Content-Type", "application/json"))
            .withBody(responseJson(fnr, aktorId))
        )

        val personOppslag =   PersonOppslag("http://localhost:${mockServer.port}", Supplier { "" })

        assertNull(personOppslag.hentFoedselsnummer(aktorId))

    }

    @Test
    fun `slaa opp med ikke-eksisterende aktorId`() {
        val aktorId = "2000003739123"
        mockServer.`when`(
            request().withPath("/pam-personoppslag/personidenter/system/oppslag/$aktorId")
        ).respond(HttpResponse.response()
            .withHeaders(Header("Content-Type", "application/json"))
            .withBody(responseJson(null, null))
        )

        val personOppslag =   PersonOppslag("http://localhost:${mockServer.port}", Supplier { "" })

        assertNull(personOppslag.hentFoedselsnummer(aktorId))
    }

    @Test
    fun `slaa opp med ikke-eksisterende fodselsnummer`() {
        val fnr = "11223312345"
        mockServer.`when`(
            request().withPath("/pam-personoppslag/personidenter/system/oppslag/$fnr")
        ).respond(HttpResponse.response()
            .withHeaders(Header("Content-Type", "application/json"))
            .withBody(responseJson(null, null))
        )

        val personOppslag =   PersonOppslag("http://localhost:${mockServer.port}", Supplier { "" })

        assertNull(personOppslag.hentAktorId(fnr))
    }

    @Test
    fun `personoppslag returnerer ikke 2xx`() {
        val fnr = "11223312345"
        mockServer.`when`(
            request().withPath("/pam-personoppslag/personidenter/system/oppslag/$fnr")
        ).respond(HttpResponse.response()
            .withStatusCode(500)
            .withHeaders(Header("Content-Type", "application/json"))
        )

        val personOppslag =   PersonOppslag("http://localhost:${mockServer.port}", Supplier { "" })

        assertNull(personOppslag.hentAktorId(fnr))
    }


    fun responseJson(fnr: String?, aktorId: String?, kandidatNummer: String? = null, arenaKandidatNummer: String? = null) =
        """
                {
                    "foedselsnummer": ${fnr?.wrapInQuotes()},
                    "aktorId": ${aktorId?.wrapInQuotes()},
                    "kandidatnummer": ${kandidatNummer?.wrapInQuotes()},
                    "arenaKandidatnummer": ${arenaKandidatNummer?.wrapInQuotes()}
                }
            """.trimIndent().also { print("Returverdi: $it") }

    fun String.wrapInQuotes() = "\"$this\""

    @AfterEach
    fun stopMockServer() {
        mockServer.stop()
    }
}