package no.nav.cv.event.cv

import io.micronaut.configuration.kafka.annotation.KafkaKey
import io.micronaut.configuration.kafka.annotation.KafkaListener
import io.micronaut.configuration.kafka.annotation.OffsetReset
import io.micronaut.configuration.kafka.annotation.Topic
import no.nav.arbeid.cv.avro.Melding
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

val log = LoggerFactory.getLogger(CvConsumer::class.java)

@KafkaListener(
        offsetReset = OffsetReset.EARLIEST
)
class CvConsumer {

    @Topic("arbeid-pam-cv-endret-v4-q0")
    fun riceive(
            @KafkaKey key: String,
            cv: Melding
    ) {
        log.info("Mottat CV")

        val sistEndret = ZonedDateTime.ofInstant(
                Instant.ofEpochMilli(cv.sistEndret.millis),
                ZoneId.of(cv.sistEndret.zone.id, ZoneId.SHORT_IDS)
        )
        log.info("CV ${key}, AktoerId: ${cv.aktoerId}, Sist endret: ${sistEndret}")
    }

}