package no.nav.cv.output

import no.nav.cv.notifikasjon.NotifikasjonAdmin
import no.nav.cv.notifikasjon.VarselPublisher
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import java.util.*

@Service
class OutboxService(
    private val outboxRepository: OutboxRepository,
    private val varselPublisher: VarselPublisher
) {
    private val log = LoggerFactory.getLogger(OutboxService::class.java)

    fun schdeuleVarsel(uuid: UUID, aktorId: String, foedselsnummer: String) =
        scheduleOutboxEntry(uuid, aktorId, foedselsnummer, OutboxEntry.OutboxEntryType.VARSLE)

    fun schdeuleDone(uuid: UUID, aktorId: String, foedselsnummer: String) =
        scheduleOutboxEntry(uuid, aktorId, foedselsnummer, OutboxEntry.OutboxEntryType.DONE)

    private fun scheduleOutboxEntry(
        uuid: UUID,
        aktorId: String,
        foedselsnummer: String,
        type: OutboxEntry.OutboxEntryType
    ) {
        val newOutboxEntry = OutboxEntry(
            uuid = uuid.toString(),
            aktorId = aktorId,
            foedselsnummer = foedselsnummer,
            type = type
        )

        log.info("Saving $newOutboxEntry to the database")

        outboxRepository.save(newOutboxEntry)
    }

    fun processQueue(noDelay: Boolean = false) {
        val olderThan = if (noDelay) ZonedDateTime.now()
        else ZonedDateTime.now().minusSeconds(30)

        outboxRepository.findAllUnprocessedMessages(olderThan)
            .also { log.info("Processing outbox with ${it.size} entries") }
            .forEach {
                processEntry(it)
            }
    }

    private fun processEntry(outboxEntry: OutboxEntry) {
        outboxEntry.processedAt = ZonedDateTime.now()

        try {
            log.info("Trying to send ${outboxEntry.type} for  ${outboxEntry.aktorId} /  ${outboxEntry.uuid}")
            when (outboxEntry.type) {
                OutboxEntry.OutboxEntryType.VARSLE -> varselPublisher.publish(
                    outboxEntry.uuid,
                    outboxEntry.foedselsnummer
                )
                OutboxEntry.OutboxEntryType.DONE -> varselPublisher.done(outboxEntry.uuid, outboxEntry.foedselsnummer)
            }

            outboxEntry.failed = false
        } catch (e: Exception) {
            log.warn(
                "Get exception while sending ${outboxEntry.type} for ${outboxEntry.aktorId} / ${outboxEntry.uuid}: ${e.message}",
                e
            )
            outboxEntry.failed = true

            if (outboxEntry.attempt < 9) {
                val nextAttempt = OutboxEntry(
                    uuid = outboxEntry.uuid,
                    aktorId = outboxEntry.aktorId,
                    foedselsnummer = outboxEntry.foedselsnummer,
                    type = outboxEntry.type,
                    attempt = outboxEntry.attempt + 1,
                    createdAt = ZonedDateTime.now()
                )

                outboxRepository.save(nextAttempt)
            }

        } finally {
            outboxRepository.save(outboxEntry)
        }
    }
}