package no.nav.cv.event.cv

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.cv.infrastructure.kafka.EventProcessor
import no.nav.cv.notifikasjon.HendelseService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

@Service
class CvEndretProcessor(
    val hendelseService: HendelseService
) : EventProcessor<String, String> {
    companion object {
        val log = LoggerFactory.getLogger(CvEndretProcessor::class.java)
        val objectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())
    }

    fun receive(record: ConsumerRecord<String, String>) {
        val cv = objectMapper.readValue<CvDto>(record.value())

        if (cv.updatedBy != UpdatedByType.PERSONBRUKER) {
            log.info("Kafka CV msg for ${cv.aktorId} is not user initiated, so skipping it")
            return
        }

        if (cv.meldingstype == CvMeldingstype.SLETT) {
            log.info("Kafka CV msg for ${cv.aktorId} is a deletion msg, so skipping it")
            return
        }

        log.info("Kafka CV msg for ${cv.aktorId} is NOT a deletion msg and was initiated by user")

        hendelseService.endretCV(cv.aktorId, cv.sistEndret())
    }

    override suspend fun process(records: ConsumerRecords<String, String>) {
        records.forEach { receive(it) }
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class CvDto(
    val aktorId: String,
    val meldingstype: CvMeldingstype,
    val cv: CvInternDto?,
    val updatedBy: UpdatedByType?
) {
    fun sistEndret() = cv?.updatedAt ?: throw Exception("Record mangler sistEndret")

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class CvInternDto(val updatedAt: ZonedDateTime?)
}

enum class CvMeldingstype { OPPRETT, ENDRE, SLETT }
enum class UpdatedByType { PERSONBRUKER, VEILEDER, SYSTEMBRUKER, SYSTEMINTERN }
