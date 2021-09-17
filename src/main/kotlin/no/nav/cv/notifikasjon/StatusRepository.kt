package no.nav.cv.notifikasjon

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.math.BigInteger
import java.time.ZonedDateTime
import java.util.*
import javax.persistence.*
import kotlin.streams.asSequence


@Repository
open class StatusRepository(
    @PersistenceContext private val entityManager: EntityManager
) {

    val serieMedWhitespace = Regex("(\\s+)")

    companion object {
        private val log = LoggerFactory.getLogger(StatusRepository::class.java)
    }

    private val oppdaterTidspunktQuery =
        """
        UPDATE STATUS
        SET TIDSPUNKT = :tidspunkt
        WHERE AKTOR_ID = :aktorId
        AND FERDIG = false
        """.replace(serieMedWhitespace, " ") // Erstatter alle serier med whitespace (feks newline) med en enkelt space



    @Transactional
    open fun oppdaterTidspunkt(aktoerId: String, tidspunkt: ZonedDateTime) {
        log.debug("Oppdaterer tidspunkt for aktør $aktoerId tidspunkt $tidspunkt")
        entityManager.createNativeQuery(oppdaterTidspunktQuery)
            .setParameter("aktorId", aktoerId)
            .setParameter("tidspunkt", tidspunkt)
            .executeUpdate()
    }

    @Transactional
    open fun lagreNyStatus(status: Status) {

        settFerdig(status.aktoerId)

        log.debug("Lagrer uuid ${status.uuid} med status ${status.status} og tidspunkt ${status.statusTidspunkt}")
        entityManager.persist(StatusEntity.fromStatus(status = status, ferdig = false))
    }

    private val settFerdigQuery =
        """
            UPDATE STATUS
            SET FERDIG = true
            WHERE AKTOR_ID = :aktorId
        """.replace(serieMedWhitespace, " ") // Erstatter alle serier med whitespace (feks newline) med en enkelt space

    @Transactional
    fun settFerdig(aktorId: String) = entityManager.createNativeQuery(settFerdigQuery)
        .setParameter("aktorId", aktorId)
        .executeUpdate()

    private val sisteQuery =
            """
        SELECT * FROM STATUS s 
        WHERE s.AKTOR_ID = :aktorId AND s.FERDIG = false
        
    """.replace(serieMedWhitespace, " ") // Erstatter alle serier med whitespace (feks newline) med en enkelt space

    @Transactional(readOnly = true)
    open fun finnSiste(aktorId: String) = entityManager.createNativeQuery(sisteQuery, StatusEntity::class.java)
        .setParameter("aktorId", aktorId)
        .resultStream
        .asSequence()
        .map { it as StatusEntity }
        .map { it.toStatus() }
        .firstOrNull()
        ?: Status.nySession(aktorId)

    private val statusPerFodselsnummer =
            """
                SELECT s.* 
                FROM STATUS s
                WHERE s.STATUS = :status
                AND s.foedselsnummer = :fodselsnummer
                AND s.ferdig = false
                ORDER BY s.TIDSPUNKT DESC               
                LIMIT 1000
            """.replace(serieMedWhitespace, " ") // Erstatter alle serier med whitespace (feks newline) med en enkelt space


    @Transactional(readOnly = true)
    open fun manglerFodselsnummer(): List<Status> {
        return entityManager.createNativeQuery(statusPerFodselsnummer, StatusEntity::class.java)
            .setParameter("status", skalVarslesManglerFnrStatus)
            .setParameter("fodselsnummer", ukjentFnr)
            .resultStream
            .asSequence()
            .map { it as StatusEntity }
            .map { it.toStatus() }
            .toList()
    }

    private val antallAvStatusQuery =
            """
                SELECT COUNT(*)
                FROM STATUS s
                WHERE s.STATUS = :status 
                AND s.ferdig = false
            """.replace(serieMedWhitespace, " ") // Erstatter alle serier med whitespace (feks newline) med en enkelt space


    open fun antallAvStatus(status: String): Long
        = (entityManager.createNativeQuery(antallAvStatusQuery)
            .setParameter("status", status)
            .singleResult as BigInteger).toLong()


    private val antallFerdigQuery =
        """
           SELECT count(*)
           FROM STATUS sVarslet, STATUS s2
           WHERE sVarslet.status = :varsletStatus
           AND s2.status = :nesteStatus
           AND sVarslet.uuid = s2.uuid
        """.replace(serieMedWhitespace, " ") // Erstatter alle serier med whitespace (feks newline) med en enkelt space

    open fun antallFerdig(nesteStatus: String): Long
            = (entityManager.createNativeQuery(antallFerdigQuery)
                .setParameter("varsletStatus", varsletStatus)
                .setParameter("nesteStatus", nesteStatus)
                .singleResult as BigInteger).toLong()
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

    @Column(name = "FERDIG", nullable = false)
    var ferdig: Boolean = true // Kan ikke ha lateinit på primitive types

    fun toStatus() = Status(
            uuid = uuid,
            aktoerId = aktorId,
            fnr = fnr,
            status = status,
            statusTidspunkt = tidspunkt)

    fun initStatus(
            uuid: UUID,
            aktorId: String,
            fnr: String,
            status: String,
            tidspunkt: ZonedDateTime,
            ferdig: Boolean
    ) {
        //this.id = 0   // Gammel kode satt denne til 0, men da får jeg org.hibernate.PersistentObjectException
                        // Når jeg ikke setter den (den er da initialisert til null), fungerer det fint,
                        // men jeg er usikker på konsekvensene? Unit testene passer
        this.uuid = uuid
        this.aktorId = aktorId
        this.fnr = fnr
        this.status = status
        this.tidspunkt = tidspunkt
        this.ferdig = ferdig
    }

    companion object {
        fun fromStatus(status: Status, ferdig: Boolean) : StatusEntity {
            val entity = StatusEntity()
            entity.initStatus(
                    uuid = status.uuid,
                    aktorId = status.aktoerId,
                    fnr = status.fnr,
                    status = status.status,
                    tidspunkt = status.statusTidspunkt,
                    ferdig = ferdig
            )
            return entity
        }

    }
}
