package no.nav.cv.repository

import io.micronaut.spring.tx.annotation.Transactional
import no.nav.cv.notifikasjon.StatusRepository
import java.time.LocalDateTime
import java.util.*
import javax.inject.Singleton
import javax.persistence.*

@Singleton
open class JpaStatusRepository(
        @PersistenceContext private val entityManager: EntityManager
): StatusRepository {


    @Transactional
    override fun lagre(status: no.nav.cv.notifikasjon.Status) {
        entityManager.persist(Status.fromStatus(status))
    }


    private val sisteQuery =
            """
        SELECT s FROM Status s 
        WHERE s.fnr = :fnr AND s.tidspunkt = (
            SELECT max(s2.tidspunkt) FROM Status s2 WHERE s2.fnr = :fnr
        )
        
    """.trimMargin()

    @Transactional(readOnly = true)
    override fun finnSiste(fnr: String): no.nav.cv.notifikasjon.Status {
        return entityManager.createQuery(sisteQuery, Status::class.java)
                .setParameter("fnr", fnr)
                .resultStream.findFirst()
                .map { it.toStatus() }
                .orElseGet { no.nav.cv.notifikasjon.Status.ukjent(fnr) }
    }

}

@Entity
@Table(name = "STATUS")
private data class Status(

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
        val tidspunkt: LocalDateTime
) {

    fun toStatus() = no.nav.cv.notifikasjon.Status(uuid, fnr, status, tidspunkt)

    companion object {
        fun fromStatus(status: no.nav.cv.notifikasjon.Status) = Status(
                    id = 0,
                    uuid = status.uuid,
                    fnr = status.fnr,
                    status = status.status,
                    tidspunkt = status.statusTidspunkt
            )
    }
}