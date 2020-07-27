package no.nav.cv.event.oppfolgingstatus

import io.micronaut.configuration.kafka.annotation.KafkaListener
import io.micronaut.configuration.kafka.annotation.OffsetReset
import io.micronaut.configuration.kafka.annotation.Topic
import no.nav.cv.notifikasjon.HendelseService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.json.JSONObject
import org.slf4j.LoggerFactory
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

//@KafkaListener(
//        groupId = "pam-brukernotifikasjon-oppfolging-v3",
//        offsetReset = OffsetReset.EARLIEST
//)
class OppfolgingsstatusConsumer(
        private val oppfolgingsService: OppfolgingstatusService
) {

    companion object {
        val log = LoggerFactory.getLogger(OppfolgingsstatusConsumer::class.java)
    }

    //@Topic("\${kafka.topics.consumers.endring_status_oppfolging}")
    fun receive(
            record: ConsumerRecord<String, String>
    ) {
        //log.debug("OppfolgingsstatusConsumer record recieved: $record")
        log.debug("OppfolgingsstatusConsumer record recieved.")
        val dto = OppfolgingstatusDto(record.value().toString())
        oppfolgingsService.oppdaterStatus(dto)
    }
}
