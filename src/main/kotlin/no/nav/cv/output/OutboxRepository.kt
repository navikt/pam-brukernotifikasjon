package no.nav.cv.output

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.ZonedDateTime
import jakarta.persistence.*

@Repository
interface OutboxRepository : JpaRepository<OutboxEntry?, Long?> {

    @Query("""
        SELECT s FROM OutboxEntry s
        WHERE s.processedAt IS NULL
        AND s.createdAt < :createdBefore
        ORDER BY s.createdAt DESC
    """)
    fun findAllUnprocessedMessages(createdBefore: ZonedDateTime): List<OutboxEntry>
}

@Entity
@Table(name = "OUTBOX")
data class OutboxEntry(
    @Id
    @GeneratedValue(generator = "OUTBOX_SEQ")
    val id: Long = 0,
    val uuid: String = "",
    val aktorId: String = "",
    val foedselsnummer: String = "",
    @Enumerated(EnumType.STRING)
    val type: OutboxEntryType? = OutboxEntryType.VARSLE,
    var attempt: Int = 0,
    var failed: Boolean = true,
    var createdAt: ZonedDateTime = ZonedDateTime.now(),
    var processedAt: ZonedDateTime? = null,
) {
    enum class OutboxEntryType {
        VARSLE,
        DONE
    }
}
