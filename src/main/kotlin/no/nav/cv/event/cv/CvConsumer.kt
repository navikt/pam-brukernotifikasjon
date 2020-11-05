package no.nav.cv.event.cv

import io.micronaut.configuration.kafka.annotation.KafkaListener
import io.micronaut.configuration.kafka.annotation.OffsetReset
import io.micronaut.configuration.kafka.annotation.Topic
import no.nav.cv.notifikasjon.HendelseService
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.ZonedDateTime
import java.util.*


@KafkaListener(
        groupId = "pam-brukernotifikasjon-cv-v4",
        offsetReset = OffsetReset.EARLIEST
)
class CvConsumer(
        val hendelseService: HendelseService
) {

    companion object {
        val log = LoggerFactory.getLogger(CvConsumer::class.java)
    }

    @Topic("\${kafka.topics.consumers.cv_endret}")
    fun receive(
            record: ConsumerRecord<String, GenericRecord>
    ) {
        val cv = CvDto(record.value())

        hendelseService.harSettCv(cv.aktorId(), cv.sistEndret())
        log.info("CV ${record.key()}, Sist endret: ${cv.sistEndret()}")
    }

}

private class CvDto(val record: GenericRecord) {
    fun aktorId() = record.aktoerId()
            ?: throw Exception("Record mangler aktorId")

    fun sistEndret() =
            ZonedDateTime.ofInstant(Instant.ofEpochMilli(sistEndretMillis()), TimeZone.getDefault().toZoneId())

    private fun sistEndretMillis() = record.sistEndret()
            ?: throw Exception("Record mangler sistEndret")
}


private fun GenericRecord.meldingstype() = get("meldingstype")?.toString() ?: "NULL"

private fun GenericRecord.opprettCv() = get("opprett_cv") as GenericRecord?

private fun GenericRecord.endretCv() = get("endre_cv") as GenericRecord?

private fun GenericRecord.slettCv() = get("slett_cv") as GenericRecord?

private fun GenericRecord.opprettJobbprofil() = get("opprett_jobbprofil") as GenericRecord?

private fun GenericRecord.endretJobbprofil() = get("endre_jobbprofil") as GenericRecord?

private fun GenericRecord.slettJobbprofil() = get("slett_jobbprofil") as GenericRecord?

private fun GenericRecord.cv() = get("cv") as GenericRecord?

private fun GenericRecord.jobbprofil() = get("jobbprofil") as GenericRecord?

private fun GenericRecord.sistEndret(): Long? = get("sist_endret") as Long?

private fun GenericRecord.aktoerId() = get("aktoer_id") as String?
