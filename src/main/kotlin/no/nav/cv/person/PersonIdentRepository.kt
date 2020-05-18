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

    companion object {
        val log = LoggerFactory.getLogger(JpaPersonIdentRepository::class.java)
    }

    private val deleteRelatedIdents = """
        DELETE PersonIdentEntity p
        WHERE p.groupId IN (
           SELECT p2.groupId FROM PersonIdentEntity p2 WHERE p2.ident IN :identVerdier)
    """.trimIndent()

    private val finnIdenter =
            """
        SELECT p FROM PersonIdentEntity p
        WHERE p.groupId IN (
            SELECT p2.groupId FROM PersonIdentEntity p2 WHERE p2.type = :type AND p2.ident = :aktorId)
    """.trimMargin()

    @Transactional
    override fun oppdater(personIdenter: PersonIdenter) {

        // Sletter alle opprinnelige identer knyttet til samme person for å kunne fjerne eventuelle sletta identer
        // TODO denne er noe sårbar for duplikater - kan kan slette identer av gal type som har samme verdi som en av de man ønsker å oppdater
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
private data class PersonIdentEntity(

        @Id
        @Column(name = "ID")
        @GeneratedValue(generator = "PERSON_IDENT_SEQ")
        private val id: Long = 0,

        @Column(name = "PERSON_ID_GROUP_ID")
        val groupId: UUID,

        @Column(name = "PERSON_ID", nullable = false)
        val ident: String,

        @Column(name = "PERSON_ID_TYPE", nullable = false, unique = true)
        val type: PersonIdent.Type,

        @Column(name = "GJELDENDE", nullable = false)
        val gjeldende: Boolean

) {

    companion object {

        fun fromPersonIdent(personIdent: PersonIdent) =
                PersonIdentEntity(0, UUID.randomUUID(), personIdent.id(), personIdent.type(), personIdent.gjeldende())

        fun fromPersonIdenter(personIdenter: PersonIdenter): List<PersonIdentEntity> {
            val uuid = UUID.randomUUID()
            return personIdenter.identer().map { PersonIdentEntity(0, uuid, it.id(), it.type(), it.gjeldende()) }
        }
    }

    fun toPersonIdent() = PersonIdent(ident, type, gjeldende)

}