package no.nav.cv.person

import io.micronaut.test.annotation.MicronautTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
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

        assertAll(
                Executable { assertEquals(hentet.identer().size, 2) },
                Executable { assertTrue(hentet.identer().containsAll(listOf(
                        PersonIdent("123", PersonIdent.Type.AKTORID, false),
                        PersonIdent("234", PersonIdent.Type.FOLKEREGISTER, true)
                ))) }
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

        assertAll(
                Executable { assertEquals(hentet.identer().size, 3) },
                Executable { assertTrue(hentet.identer().containsAll(listOf(
                        PersonIdent("333", PersonIdent.Type.FOLKEREGISTER, true)
                ))) }
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

        assertAll(
                Executable { assertEquals(hentet.identer().size, 3) },
                Executable { assertTrue(hentet.identer().containsAll(listOf(
                        PersonIdent("333", PersonIdent.Type.AKTORID, true)
                ))) }
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

        assertEquals(hentetPersonOpprinnelig.identer().size, 0)

        assertAll(
                Executable { assertEquals(hentetPerson1.identer().size, 2) },
                Executable { assertTrue(hentetPerson1.identer().contains(
                        PersonIdent("222", PersonIdent.Type.FOLKEREGISTER, true)
                )) }
        )

        assertAll(
                Executable { assertEquals(2, hentetPerson2.identer().size) },
                Executable { assertTrue(hentetPerson2.identer().contains(
                        PersonIdent("333", PersonIdent.Type.FOLKEREGISTER, true)
                )) }
        )

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

        assertAll(
                Executable { assertEquals(hentetSomOpprinneligPerson1.identer().size, 4) },
                Executable { assertTrue(hentetSomOpprinneligPerson1.identer().containsAll(listOf(
                        PersonIdent("111", PersonIdent.Type.AKTORID, true),
                        PersonIdent("333", PersonIdent.Type.AKTORID, false),
                        PersonIdent("222", PersonIdent.Type.FOLKEREGISTER, false),
                        PersonIdent("444", PersonIdent.Type.FOLKEREGISTER, true)
                ))) }
        )

        assertAll(
                Executable { assertEquals(hentetSomOpprinneligPerson2.identer().size, 4) },
                Executable { assertTrue(hentetSomOpprinneligPerson2.identer().containsAll(listOf(
                        PersonIdent("111", PersonIdent.Type.AKTORID, true),
                        PersonIdent("333", PersonIdent.Type.AKTORID, false),
                        PersonIdent("222", PersonIdent.Type.FOLKEREGISTER, false),
                        PersonIdent("444", PersonIdent.Type.FOLKEREGISTER, true)
                ))) }
        )


    }
}