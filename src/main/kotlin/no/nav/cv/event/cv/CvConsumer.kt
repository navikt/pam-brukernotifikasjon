package no.nav.cv.event.cv

import io.micronaut.configuration.kafka.annotation.KafkaListener
import io.micronaut.configuration.kafka.annotation.OffsetReset
import io.micronaut.configuration.kafka.annotation.Topic
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.ZonedDateTime
import java.util.*


@KafkaListener(
        groupId = "pam-brukernotifikasjon",
        offsetReset = OffsetReset.EARLIEST
)
class CvConsumer {

    companion object {
        val log = LoggerFactory.getLogger(CvConsumer::class.java)
    }

    @Topic("arbeid-pam-cv-endret-v4-q0")
    fun receive(
            record: ConsumerRecord<String, GenericRecord>
    ) {
        log.info("Mottat CV - topic: ${record.topic()} Partition ${record.partition()}, offset: ${record.offset()}, timestamp:  ${record.timestamp()}")

        val sistEndretMillis = record.value()
                .endretCv()
                .cv()
                .get("sist_endret").toString()
        val aktoerId = record.value().get("aktoerId").toString()
        log.info("${aktoerId}: ${sistEndretMillis}")
        val sistEndret = ZonedDateTime.ofInstant(Instant.ofEpochMilli(sistEndretMillis.toLong()), TimeZone.getDefault().toZoneId())

        log.info("CV ${record.key()}, AktoerId: ${aktoerId}, Sist endret: ${sistEndret}")
    }

}

private fun GenericRecord.opprettCv(): GenericRecord { return get("opprett_cv") as GenericRecord }
private fun GenericRecord.endretCv(): GenericRecord { return get("endre_cv") as GenericRecord }
private fun GenericRecord.slettCv(): GenericRecord { return get("slett_cv") as GenericRecord }
private fun GenericRecord.opprettJobbprofil(): GenericRecord { return get("opprett_jobbprofil") as GenericRecord }
private fun GenericRecord.endretJobbprofil(): GenericRecord { return get("endre_jobbprofil") as GenericRecord }
private fun GenericRecord.slettJobbprofil(): GenericRecord { return get("slett_jobbprofil") as GenericRecord }

private fun GenericRecord.cv(): GenericRecord { return get("cv") as GenericRecord }
private fun GenericRecord.jobbprofil(): GenericRecord { return get("jobbprofil") as GenericRecord }