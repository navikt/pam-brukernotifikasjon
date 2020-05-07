package no.nav.cv.event.cv

import io.micronaut.configuration.kafka.annotation.KafkaListener
import io.micronaut.configuration.kafka.annotation.OffsetReset
import io.micronaut.configuration.kafka.annotation.Topic
import no.nav.arbeid.cv.avro.Melding
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime



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
            record: ConsumerRecord<String, Melding>
    ) {
        log.info("Mottat CV - topic: ${record.topic()} Partition ${record.partition()}, offset: ${record.offset()}, timestamp:  ${record.timestamp()}")

        val sistEndret = ZonedDateTime.ofInstant(
                Instant.ofEpochMilli(record.value().sistEndret.millis),
                ZoneId.of(record.value().sistEndret.zone.id, ZoneId.SHORT_IDS)
        )
        log.info("CV ${record.key()}, AktoerId: ${record.value().aktoerId}, Sist endret: ${sistEndret}")
    }

}