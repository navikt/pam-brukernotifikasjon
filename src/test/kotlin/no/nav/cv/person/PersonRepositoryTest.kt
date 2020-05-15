package no.nav.cv.person

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import io.micronaut.test.annotation.MicronautTest
import org.junit.jupiter.api.Test
import javax.inject.Inject

@MicronautTest
class PersonRepositoryTest {

    @Inject
    lateinit var personIdentRepository: PersonIdentRepository


    @Test
    fun `finn identer knyttet til aktorid`() {

        personIdentRepository.oppdater(PersonIdenter(listOf(
                PersonIdent("123", PersonIdent.Type.AKTORID, false),
                PersonIdent("234", PersonIdent.Type.FOLKEREGISTER, true)
        )))
        val hentet = personIdentRepository.finnIdenter("123")

        assertThat(hentet.identer().size).isEqualTo(2)
        assertThat(hentet.identer()).containsExactly(
                PersonIdent("123", PersonIdent.Type.AKTORID, false),
                PersonIdent("234", PersonIdent.Type.FOLKEREGISTER, true)
        )
    }

    @Test
    fun `nytt fodselsnummer blir inkludert`() {

        personIdentRepository.oppdater(PersonIdenter(listOf(
                PersonIdent("111", PersonIdent.Type.AKTORID, false),
                PersonIdent("222", PersonIdent.Type.FOLKEREGISTER, true)
        )))
        personIdentRepository.oppdater(PersonIdenter(listOf(
                PersonIdent("111", PersonIdent.Type.AKTORID, false),
                PersonIdent("222", PersonIdent.Type.FOLKEREGISTER, false),
                PersonIdent("333", PersonIdent.Type.FOLKEREGISTER, true)
        )))

        val hentet = personIdentRepository.finnIdenter("111")

        assertThat(hentet.identer().size).isEqualTo(3)
        assertThat(hentet.identer()).contains(
                PersonIdent("333", PersonIdent.Type.FOLKEREGISTER, true)
        )
    }

    @Test
    fun `ny aktorid blir inkludert`() {

        personIdentRepository.oppdater(PersonIdenter(listOf(
                PersonIdent("111", PersonIdent.Type.AKTORID, false),
                PersonIdent("222", PersonIdent.Type.FOLKEREGISTER, true)
        )))
        personIdentRepository.oppdater(PersonIdenter(listOf(
                PersonIdent("111", PersonIdent.Type.AKTORID, false),
                PersonIdent("222", PersonIdent.Type.FOLKEREGISTER, false),
                PersonIdent("333", PersonIdent.Type.AKTORID, true)
        )))

        val hentet = personIdentRepository.finnIdenter("111")

        assertThat(hentet.identer().size).isEqualTo(3)
        assertThat(hentet.identer()).contains(
                PersonIdent("333", PersonIdent.Type.AKTORID, true)
        )
    }

    @Test
    fun `person splitt blir handtert`() {

        personIdentRepository.oppdater(PersonIdenter(listOf(
                PersonIdent("111", PersonIdent.Type.AKTORID, false),
                PersonIdent("222", PersonIdent.Type.FOLKEREGISTER, false),
                PersonIdent("333", PersonIdent.Type.FOLKEREGISTER, true)
        )))
        personIdentRepository.oppdater(PersonIdenter(listOf(
                PersonIdent("555", PersonIdent.Type.AKTORID, false),
                PersonIdent("222", PersonIdent.Type.FOLKEREGISTER, true)
        )))
        personIdentRepository.oppdater(PersonIdenter(listOf(
                PersonIdent("666", PersonIdent.Type.AKTORID, false),
                PersonIdent("333", PersonIdent.Type.FOLKEREGISTER, true)
        )))

        val hentetPersonOpprinnelig = personIdentRepository.finnIdenter("111")
        val hentetPerson1 = personIdentRepository.finnIdenter("555")
        val hentetPerson2 = personIdentRepository.finnIdenter("666")

        assertThat(hentetPersonOpprinnelig.identer().size).isEqualTo(0)

        assertThat(hentetPerson1.identer().size).isEqualTo(2)
        assertThat(hentetPerson1.identer()).contains(
                PersonIdent("222", PersonIdent.Type.FOLKEREGISTER, true))

        assertThat(hentetPerson2.identer().size).isEqualTo(2)
        assertThat(hentetPerson2.identer()).contains(
                PersonIdent("333", PersonIdent.Type.FOLKEREGISTER, true))
    }


    @Test
    fun `person merge blir handtert`() {

        personIdentRepository.oppdater(PersonIdenter(listOf(
                PersonIdent("111", PersonIdent.Type.AKTORID, true),
                PersonIdent("222", PersonIdent.Type.FOLKEREGISTER, true)
        )))
        personIdentRepository.oppdater(PersonIdenter(listOf(
                PersonIdent("333", PersonIdent.Type.AKTORID, true),
                PersonIdent("444", PersonIdent.Type.FOLKEREGISTER, true)
        )))
        personIdentRepository.oppdater(PersonIdenter(listOf(
                PersonIdent("111", PersonIdent.Type.AKTORID, true),
                PersonIdent("333", PersonIdent.Type.AKTORID, false),
                PersonIdent("222", PersonIdent.Type.FOLKEREGISTER, false),
                PersonIdent("444", PersonIdent.Type.FOLKEREGISTER, true)
        )))

        val hentetSomOpprinneligPerson1 = personIdentRepository.finnIdenter("111")
        val hentetSomOpprinneligPerson2 = personIdentRepository.finnIdenter("333")

        assertThat(hentetSomOpprinneligPerson1.identer().size).isEqualTo(4)
        assertThat(hentetSomOpprinneligPerson1.identer()).containsExactly(
                PersonIdent("111", PersonIdent.Type.AKTORID, true),
                PersonIdent("333", PersonIdent.Type.AKTORID, false),
                PersonIdent("222", PersonIdent.Type.FOLKEREGISTER, false),
                PersonIdent("444", PersonIdent.Type.FOLKEREGISTER, true))

        assertThat(hentetSomOpprinneligPerson2.identer().size).isEqualTo(4)
        assertThat(hentetSomOpprinneligPerson2.identer()).containsExactly(
                PersonIdent("111", PersonIdent.Type.AKTORID, true),
                PersonIdent("333", PersonIdent.Type.AKTORID, false),
                PersonIdent("222", PersonIdent.Type.FOLKEREGISTER, false),
                PersonIdent("444", PersonIdent.Type.FOLKEREGISTER, true))

    }
}