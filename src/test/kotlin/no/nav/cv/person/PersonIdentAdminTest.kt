package no.nav.cv.person

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.micronaut.context.annotation.Property
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest
import io.micronaut.test.annotation.MockBean
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import javax.inject.Inject

@MicronautTest
class PersonIdentAdminTest {

    @Inject @field:Client("/pam-brukernotifikasjon") lateinit var client: RxHttpClient

    @Inject
    lateinit var personIdentRepository: PersonIdentRepository

    @Test
    @Property(name="personIdent.admin.enabled", value="enabled")
    fun `that admin is enabled by property` () {
        assertThat(client.toBlocking()
                .exchange<String>("/internal/addIdent/123/321")
                .status
        ).isEqualTo(HttpStatus.OK)
    }

    @MockBean(bean = PersonIdentRepository::class)
    fun personIdentRepository()
        = mockk<PersonIdentRepository>(relaxed = true)

}

@MicronautTest
class PersonIdentAdminDisabledTest {

    @Inject @field:Client("/pam-brukernotifikasjon") lateinit var client: RxHttpClient

    @Test
    @Property(name="personIdent.admin.enabled", value="disabled")
    fun `that admin is disabled by property` () {
        assertThrows<HttpClientResponseException> {
            client.toBlocking()
                    .exchange<Any>("/internal/addIdent/123/321")
        }
    }

    @MockBean(bean = PersonIdentRepository::class)
    fun personIdentRepository()
        = mockk<PersonIdentRepository>(relaxed = true)

}