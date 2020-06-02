package no.nav.cv.event.oppfolgingstatus

import io.micronaut.configuration.kafka.annotation.KafkaListener
import io.micronaut.configuration.kafka.annotation.OffsetReset
import io.micronaut.configuration.kafka.annotation.Topic
import no.nav.cv.notifikasjon.HendelseService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.json.JSONObject
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@KafkaListener(
        groupId = "pam-brukernotifikasjon-oppfolging",
        offsetReset = OffsetReset.EARLIEST
)
class OppfolgingsstatusConsumer(
        private val hendelseService: HendelseService
) {

    @Topic("\${kafka.topics.consumers.endring_status_oppfolging}")
    fun receive(
            record: ConsumerRecord<String, String>
    ) {
        val dto = OppfolgingstatusDto(record.value().toString())

        if(dto.underOppfolging()) {
            hendelseService.kommetUnderOppfolging(dto.aktorId(), dto.startForSisteOppfolgningsPeriode())
        } else {
            hendelseService.blittFulgtOpp(dto.aktorId(), dto.endret())
        }
    }

}

private class OppfolgingstatusDto(
        json: String
) {
    private val json: JSONObject = JSONObject(json)

    fun aktorId() = json.getString("aktoerid")

    fun underOppfolging() = json.getBoolean("oppfolging")

    fun startForSisteOppfolgningsPeriode() = ZonedDateTime.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(json.getString("startDato")))

    fun endret() = ZonedDateTime.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(json.getString("endretTimestamp")))

}