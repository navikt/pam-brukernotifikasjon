package no.nav.cv.notifikasjon

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import io.micronaut.context.annotation.Property
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.annotation.MicronautTest
import io.micronaut.test.annotation.MockBean
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.util.*
import javax.inject.Inject


@MicronautTest
class PersonIdentAdminTest {

    private val uuid = UUID.randomUUID()
    private val fnr = "123dummy123"

    @Inject
    @field:Client("/pam-brukernotifikasjon") lateinit var client: RxHttpClient

    @Inject
    lateinit var varselPublisher: VarselPublisher

    @Test
    fun `varsel blir sendt` () {
        val uuidSlot = slot<UUID>()
        val fnrSlot = slot<String>()

        client.toBlocking().retrieve(
                HttpRequest.create<String>(HttpMethod.POST, "/internal/kafka/manuell/varsel/${uuid}/${fnr}"))

        verify { varselPublisher.publish(capture(uuidSlot), capture(fnrSlot)) }

        assertThat(uuidSlot.captured).isEqualTo(uuid)
        assertThat(fnrSlot.captured).isEqualTo(fnr)

    }

    @Test
    fun `done blir sendt` () {
        val uuidSlot = slot<UUID>()
        val fnrSlot = slot<String>()

        client.toBlocking().retrieve(
                HttpRequest.create<String>(HttpMethod.POST, "/internal/kafka/manuell/donemelding/${uuid}/${fnr}"))

        verify { varselPublisher.done(capture(uuidSlot), capture(fnrSlot)) }

        assertThat(uuidSlot.captured).isEqualTo(uuid)
        assertThat(fnrSlot.captured).isEqualTo(fnr)

    }

    @Test
    fun `done blir sendt med systembruker` () {
        val systembruker = "enSystemBruker"
        val uuidSlot = slot<UUID>()
        val fnrSlot = slot<String>()
        val systembrukerSlot = slot<String>()

        client.toBlocking().retrieve(
                HttpRequest.create<String>(HttpMethod.POST, "/internal/kafka/manuell/donemelding/${uuid}/${fnr}/${systembruker}"))

        verify { varselPublisher.done(capture(uuidSlot), capture(fnrSlot), capture(systembrukerSlot)) }

        assertThat(uuidSlot.captured).isEqualTo(uuid)
        assertThat(fnrSlot.captured).isEqualTo(fnr)
        assertThat(systembrukerSlot.captured).isEqualTo(systembruker)

    }

    @MockBean(bean = VarselPublisher::class)
    fun varselPublisher() = mockk<VarselPublisher>(relaxed = true)

}