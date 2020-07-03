package no.nav.cv.person

import io.micronaut.spring.tx.annotation.Transactional
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Singleton
import javax.persistence.*


interface PersonIdentRepository {

    fun oppdater(personIdenter: PersonIdenter)

    fun finnIdenter(aktorId: String): PersonIdenter

}

@Singleton
private open class JpaPersonIdentRepository(
        @PersistenceContext private val entityManager: EntityManager
): PersonIdentRepository {

    val serieMedWhitespace = Regex("(\\s+)")

    companion object {
        val log = LoggerFactory.getLogger(JpaPersonIdentRepository::class.java)
    }

    private val deleteRelatedIdents = """
        DELETE PersonIdentEntity p
        WHERE p.groupId IN (
           SELECT p2.groupId FROM PersonIdentEntity p2 WHERE p2.ident IN :identVerdier)
    """.replace(serieMedWhitespace, " ") // Erstatter alle serier med whitespace (feks newline) med en enkelt space

    private val finnIdenter =
            """
        SELECT p FROM PersonIdentEntity p
        WHERE p.groupId IN (
            SELECT p2.groupId FROM PersonIdentEntity p2 WHERE p2.type = :type AND p2.ident = :aktorId)
    """.replace(serieMedWhitespace, " ") // Erstatter alle serier med whitespace (feks newline) med en enkelt space

    @Transactional
    override fun oppdater(personIdenter: PersonIdenter) {

        // Sletter alle opprinnelige identer knyttet til samme person for å kunne fjerne eventuelle sletta identer
        entityManager.createQuery(deleteRelatedIdents)
                .setParameter("identVerdier", personIdenter.identerVerdier())
                .executeUpdate()

        // Lagre så alle mottatt identer
        PersonIdentEntity.fromPersonIdenter(personIdenter).forEach {
            entityManager.persist(it)
        }
    }

    @Transactional(readOnly = true)
    override fun finnIdenter(aktorId: String): PersonIdenter {
        return entityManager.createQuery(finnIdenter, PersonIdentEntity::class.java)
                .setParameter("type", PersonIdent.Type.AKTORID)
                .setParameter("aktorId", aktorId)
                .resultList
                .map { it.toPersonIdent() }
                .let { PersonIdenter(it) }
    }

}

@Entity
@Table(name = "PERSON_IDENT")
private class PersonIdentEntity {

        @Id
        @Column(name = "ID")
        @GeneratedValue(generator = "PERSON_IDENT_SEQ")
        private val id: Long = 0

        @Column(name = "PERSON_ID_GROUP_ID")
        lateinit var groupId: UUID

        @Column(name = "PERSON_ID", nullable = false)
        lateinit var ident: String

        @Enumerated(EnumType.STRING)
        @Column(name = "PERSON_ID_TYPE", nullable = false, unique = true)
        lateinit var type: PersonIdent.Type

        @Column(name = "GJELDENDE", nullable = false)
        var gjeldende: Boolean? = null  // Denne er nullable i @Column annotation. Antar det blir en excption dersom
                                        // verdien ikke er satt, men det er vel for så vidt greit?

    fun initStatus(groupId: UUID, ident: String, type: PersonIdent.Type, gjeldende: Boolean) : PersonIdentEntity {
        this.groupId = groupId
        this.ident = ident
        this.type = type
        this.gjeldende = gjeldende

        return this
    }

    companion object {

        fun fromPersonIdent(personIdent: PersonIdent) {
            val entity = PersonIdentEntity()
            entity.initStatus(UUID.randomUUID(), personIdent.id(), personIdent.type(), personIdent.gjeldende())
        }

        fun fromPersonIdenter(personIdenter: PersonIdenter): List<PersonIdentEntity> {
            val uuid = UUID.randomUUID()
            return personIdenter.identer().map { PersonIdentEntity().initStatus(uuid, it.id(), it.type(), it.gjeldende()) }
        }
    }

    // Føles veldig skittent å bruke '!!'. Kan evt wrappe det i en get metode som kaster en exception vi har kontroll på
    fun toPersonIdent() = PersonIdent(ident, type, gjeldende!!)

}