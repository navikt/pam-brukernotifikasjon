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
        AND s.FORTSETT_TIDSPUNKT <:fortsettTidspunkt 
        
    """.replace(serieMedWhitespace, " ") // Erstatter alle serier med whitespace (feks newline) med en enkelt space

    @Transactional(readOnly = true)
    open fun skalVarsles(): List<Status> {
        val now = ZonedDateTime.now()
        return entityManager.createNativeQuery(skalVarsles, StatusEntity::class.java)
                .setParameter("status", skalVarslesStatus)
                .setParameter("fortsettTidspunkt", now)
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
    //@Convert(converter = ZonedDateTimeConverter::class)
    lateinit var tidspunkt: ZonedDateTime

    @Column(name = "FORTSETT_TIDSPUNKT", nullable = false)
    //@Convert(converter = ZonedDateTimeConverter::class)
    lateinit var fortsettTidspunkt: ZonedDateTime

    fun toStatus() = Status(
            uuid = uuid,
            aktorId = aktorId,
            fnr = fnr,
            status = status,
            statusTidspunkt = tidspunkt,
            fortsettTidspunkt = fortsettTidspunkt)

    fun initStatus(
            uuid: UUID,
            aktorId: String,
            fnr: String,
            status: String,
            tidspunkt: ZonedDateTime,
            fortsettTidspunkt: ZonedDateTime) {
        //this.id = 0   // Gammel kode satt denne til 0, men da får jeg org.hibernate.PersistentObjectException
                        // Når jeg ikke setter den (den er da initialisert til null), fungerer det fint,
                        // men jeg er usikker på konsekvensene? Unit testene passer
        this.uuid = uuid
        this.aktorId = aktorId
        this.fnr = fnr
        this.status = status
        this.tidspunkt = tidspunkt
        this.fortsettTidspunkt = fortsettTidspunkt
    }

    companion object {
        fun fromStatus(status: Status) : StatusEntity {
            val entity = StatusEntity()
            entity.initStatus(
                    uuid = status.uuid,
                    aktorId = status.aktorId,
                    fnr = status.fnr,
                    status = status.status,
                    tidspunkt = status.statusTidspunkt,
                    fortsettTidspunkt = status.fortsettTidspunkt
            )
            return entity
        }

    }
}


//@Converter(autoApply = true)
//class ZonedDateTimeConverter : AttributeConverter<ZonedDateTime?, Calendar?> {
//    override fun convertToDatabaseColumn(entityAttribute: ZonedDateTime?): Calendar? {
//        if (entityAttribute == null) {
//            return null
//        }
//        val calendar = Calendar.getInstance()
//        calendar.timeInMillis = entityAttribute.toInstant().toEpochMilli()
//        calendar.timeZone = TimeZone.getTimeZone(entityAttribute.zone)
//        return calendar
//    }
//
//    override fun convertToEntityAttribute(databaseColumn: Calendar?): ZonedDateTime? {
//        return if (databaseColumn == null) {
//            null
//        } else ZonedDateTime.ofInstant(databaseColumn.toInstant(),
//                databaseColumn
//                        .timeZone
//                        .toZoneId())
//    }
//}