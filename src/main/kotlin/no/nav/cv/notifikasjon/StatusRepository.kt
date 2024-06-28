package no.nav.cv.notifikasjon

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityManager
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.PersistenceContext
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.math.BigInteger
import java.time.ZonedDateTime
import java.util.*
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

    private val alleForAktørIdQuery = """
        SELECT * FROM STATUS s 
        WHERE s.AKTOR_ID = :aktorId
    """.trimIndent()

    @Transactional(readOnly = true)
    open fun finnAlleForAktørId(aktorId: String): List<Status> {
        return entityManager.createNativeQuery(alleForAktørIdQuery, StatusEntity::class.java)
            .setParameter("aktorId", aktorId).resultStream.asSequence().map { it as StatusEntity }.map { it.toStatus() }
            .toList()
    }

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

    private val statusMedÅpneVarsler = """SELECT s.* FROM STATUS s WHERE s.status = 'varslet' AND s.ferdig = false""".trimIndent()

    @Transactional(readOnly = true)
    fun finnAlleMedÅpneVarsler(): List<Status> {
        return entityManager.createNativeQuery(statusMedÅpneVarsler, StatusEntity::class.java)
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
            .singleResult as Long)


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
                .singleResult as Long)

    private val conversionRateWeeklyQuery =
        """
            select
                trunc(random() * 100000000) as id,
                extract(year from sStart.tidspunkt) as "year",
                extract(week from sStart.tidspunkt) as week,
                sEnd.status as status,
                count(*) as count
            from status sStart, status sEnd
            where sStart.uuid = sEnd.uuid
              AND sEnd.status in ('varslet', 'cvOppdatert', 'ikkeUnderOppfølging')
              AND sStart.status = 'varslet'
              AND sEnd.id = (select max(sId.id) from status sId where sId.uuid = sStart.uuid)
            group by "year", week, sEnd.status
        """.replace(serieMedWhitespace, " ") // Erstatter alle serier med whitespace (feks newline) med en enkelt space

    open fun conversionRateWeekly()
        = entityManager.createNativeQuery(conversionRateWeeklyQuery, ConversionRateWeekEntity::class.java)
        .resultList
        .map { it as ConversionRateWeekEntity}
        .map { it.toDto() }

}

@Entity
private class ConversionRateWeekEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private var id: Int = 0

    @Column(name = "YEAR")
    private var year: Long = 0

    @Column(name = "WEEK")
    private var week: Long = 0

    @Column(name = "STATUS")
    lateinit var status: String

    @Column(name = "COUNT")
    private var count: Long = 0

    fun toDto() = ConversionRateWeek(
        year = year,
        week = week,
        status = status,
        count = count
    )
}

@Serializable
data class ConversionRateWeek(
    val year: Long,
    val week: Long,
    val status: String,
    val count: Long
)

@Entity
@Table(name = "STATUS")
private class StatusEntity() {

    @Id
    @Column(name = "ID")
    @GeneratedValue(generator = "STATUS_SEQ", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "STATUS_SEQ", sequenceName = "STATUS_SEQ", allocationSize = 1)
    private var id: Long? = null // Kan ikke ha lateinit på primitive types

    @Column(name = "UUID", nullable = false, unique = true)
    lateinit var uuid: String

    @Column(name = "AKTOR_ID", nullable = false)
    lateinit var aktorId: String

    @Column(name = "FOEDSELSNUMMER", nullable = false)
    lateinit var fnr: String

    @Column(name = "STATUS", nullable = false)
    lateinit var status: String

    // TODO: Endre TIDSPUNKT fra å være timestamp til å være timestamptz, slik at vi faktisk får lagret tidssonen.
    @Column(name = "TIDSPUNKT", nullable = false)
    lateinit var tidspunkt: ZonedDateTime

    @Column(name = "FERDIG", nullable = false)
    var ferdig: Boolean = true // Kan ikke ha lateinit på primitive types

    fun toStatus() = Status(
            uuid = UUID.fromString(uuid),
            aktoerId = aktorId,
            fnr = fnr,
            status = status,
            statusTidspunkt = tidspunkt)

    fun initStatus(
            uuid: String,
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
                    uuid = status.uuid.toString(),
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
