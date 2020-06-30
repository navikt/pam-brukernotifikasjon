package no.nav.cv.notifikasjon

import io.micronaut.spring.tx.annotation.Transactional
import org.slf4j.LoggerFactory
import java.time.ZonedDateTime
import java.util.*
import javax.inject.Singleton
import javax.persistence.*
import kotlin.streams.toList


@Singleton
open class StatusRepository(
        @PersistenceContext private val entityManager: EntityManager
) {

    val serieMedWhitespace = Regex("(\\s+)")

    companion object {
        private val log = LoggerFactory.getLogger(StatusRepository::class.java)
    }


    @Transactional
    open fun lagre(status: Status) {
        entityManager.persist(StatusEntity.fromStatus(status))
    }


    private val sisteQuery =
            """
        SELECT * FROM STATUS s 
        WHERE s.AKTOR_ID = :aktorId AND s.TIDSPUNKT = (
            SELECT max(s2.TIDSPUNKT) FROM STATUS s2 WHERE s2.AKTOR_ID = :aktorId
        )
        
    """.replace(serieMedWhitespace, " ") // Erstatter alle serier med whitespace (feks newline) med en enkelt space

    @Transactional(readOnly = true)
    open fun finnSiste(aktorId: String): Status {
        return entityManager.createNativeQuery(sisteQuery, StatusEntity::class.java)
                .setParameter("aktorId", aktorId)
                .resultStream.findFirst()
                .map { it as StatusEntity }
                .map { it.toStatus() }
                .orElseGet { Status.nyBruker(aktorId) }
    }

    private val skalVarsles =
            """
        SELECT *
        FROM STATUS s INNER JOIN 
         (
            SELECT max(s_inner.TIDSPUNKT) tidspunkt, s_inner.AKTOR_ID
            FROM STATUS s_inner GROUP BY s_inner.AKTOR_ID
        ) as s2 on s2.TIDSPUNKT = s.TIDSPUNKT AND s2.AKTOR_ID = s.AKTOR_ID
        WHERE s.STATUS = :status 
        AND s.foedselsnummer != :ukjentFnr
        ORDER BY s.TIDSPUNKT
        LIMIT 10000
    """.replace(serieMedWhitespace, " ") // Erstatter alle serier med whitespace (feks newline) med en enkelt space

    @Transactional(readOnly = true)
    open fun skalVarsles(): List<Status> {
        val now = ZonedDateTime.now()
        return entityManager.createNativeQuery(skalVarsles, StatusEntity::class.java)
                .setParameter("status", skalVarslesStatus)
                .setParameter("ukjentFnr", ukjentFnr)
                .resultStream
                .map { it as StatusEntity }
                .map { it.toStatus() }
                .toList()
    }

    private val statusPerFodselsnummer =
            """
                SELECT * 
                FROM STATUS s
                WHERE s.STATUS = :status
                AND s.foedselsnummer = :fodselsnummer
                ORDER BY s.TIDSPUNKT DESC               
                LIMIT 1000
            """.replace(serieMedWhitespace, " ") // Erstatter alle serier med whitespace (feks newline) med en enkelt space


    @Transactional(readOnly = true)
    open fun manglerFodselsnummer(): List<Status> {
        return entityManager.createNativeQuery(statusPerFodselsnummer, StatusEntity::class.java)
                .setParameter("status", skalVarslesStatus)
                .setParameter("fodselsnummer", ukjentFnr)
                .resultStream
                .map { it as StatusEntity }
                .map { it.toStatus() }
                .toList()
    }
}

@Entity
@Table(name = "STATUS")
private class StatusEntity() {

    @Id
    @Column(name = "ID")
    @GeneratedValue(generator = "STATUS_SEQ")
    private var id: Long? = null // Kan ikke ha lateinit på primitive types

    @Column(name = "UUID", nullable = false, unique = true)
    lateinit var uuid: UUID

    @Column(name = "AKTOR_ID", nullable = false)
    lateinit var aktorId: String

    @Column(name = "FOEDSELSNUMMER", nullable = false)
    lateinit var fnr: String

    @Column(name = "STATUS", nullable = false)
    lateinit var status: String

    @Column(name = "TIDSPUNKT", nullable = false)
    lateinit var tidspunkt: ZonedDateTime

    fun toStatus() = Status(
            uuid = uuid,
            aktorId = aktorId,
            fnr = fnr,
            status = status,
            statusTidspunkt = tidspunkt)

    fun initStatus(
            uuid: UUID,
            aktorId: String,
            fnr: String,
            status: String,
            tidspunkt: ZonedDateTime
    ) {
        //this.id = 0   // Gammel kode satt denne til 0, men da får jeg org.hibernate.PersistentObjectException
                        // Når jeg ikke setter den (den er da initialisert til null), fungerer det fint,
                        // men jeg er usikker på konsekvensene? Unit testene passer
        this.uuid = uuid
        this.aktorId = aktorId
        this.fnr = fnr
        this.status = status
        this.tidspunkt = tidspunkt
    }

    companion object {
        fun fromStatus(status: Status) : StatusEntity {
            val entity = StatusEntity()
            entity.initStatus(
                    uuid = status.uuid,
                    aktorId = status.aktorId,
                    fnr = status.fnr,
                    status = status.status,
                    tidspunkt = status.statusTidspunkt
            )
            return entity
        }

    }
}
