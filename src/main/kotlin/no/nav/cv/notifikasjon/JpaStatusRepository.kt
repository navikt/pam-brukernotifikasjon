package no.nav.cv.notifikasjon

import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime
import java.util.*
import javax.inject.Singleton
import javax.persistence.*

@Singleton
open class JpaStatusRepository(
        @PersistenceContext private val entityManager: EntityManager
): StatusRepository {


    @Transactional
    override fun lagre(status: Status) {
        entityManager.persist(StatusEntity.fromStatus(status))
    }


    private val sisteQuery =
            """
        SELECT s FROM StatusEntity s 
        WHERE s.fnr = :fnr AND s.tidspunkt = (
            SELECT max(s2.tidspunkt) FROM StatusEntity s2 WHERE s2.fnr = :fnr
        )
        
    """.trimMargin()

    @Transactional(readOnly = true)
    override fun finnSiste(fnr: String): Status {
        return entityManager.createQuery(sisteQuery, StatusEntity::class.java)
                .setParameter("fnr", fnr)
                .resultStream.findFirst()
                .map { it.toStatus() }
                .orElseGet { Status.ukjent(fnr) }
    }

}

@Entity
@Table(name = "STATUS")
private data class StatusEntity(

        @Id
        @Column(name = "ID")
        @GeneratedValue(generator = "STATUS_SEQ")
        private val id: Long,

        @Column(name = "UUID", nullable = false, unique = true)
        val uuid: UUID,

        @Column(name = "FOEDSELSNUMMER", nullable = false)
        val fnr: String,

        @Column(name = "STATUS", nullable = false)
        val status: String,

        @Column(name = "TIDSPUNKT", nullable = false)
        val tidspunkt: ZonedDateTime
) {

    fun toStatus() = Status(uuid, fnr, status, tidspunkt)

    companion object {
        fun fromStatus(status: Status) = StatusEntity(
                id = 0,
                uuid = status.uuid,
                fnr = status.fnr,
                status = status.status,
                tidspunkt = status.statusTidspunkt
        )
    }
}