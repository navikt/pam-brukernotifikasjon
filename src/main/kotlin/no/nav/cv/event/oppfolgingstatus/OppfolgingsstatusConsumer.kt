package no.nav.cv.event.oppfolgingstatus

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import io.micronaut.configuration.kafka.annotation.KafkaListener
import io.micronaut.configuration.kafka.annotation.OffsetReset
import io.micronaut.configuration.kafka.annotation.Topic
import no.nav.cv.notifikasjon.HendelseService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory

@KafkaListener(
    groupId = "pam-brukernotifikasjon-oppfolging-v1",
    offsetReset = OffsetReset.EARLIEST
)
class OppfolgingsstatusConsumer(
    private val hendelseService: HendelseService
) {

    private val log = LoggerFactory.getLogger(OppfolgingsstatusConsumer::class.java)

    @Topic("\${kafka.topics.consumers.oppfolging_startet}")
    fun receiveBegun(record: ConsumerRecord<String, String>) = record.value()
        .map { json -> Json.decodeFromString<OppfolgingStartet>(json.toString()) }
        .onEach { log.debug("OppfolgingStartet record received for ${it.aktorId}.") }
        .onEach { dto -> hendelseService.kommetUnderOppfolging(dto.aktorId, dto.oppfolgingStartet) }

    @Topic("\${kafka.topics.consumers.oppfolging_avsluttet}")
    fun receiveFinished(record: ConsumerRecord<String, String>) = record.value()
        .map { json -> Json.decodeFromString<OppfolgingAvsluttet>(json.toString()) }
        .onEach { log.debug("OppfolgingAvsluttet record received for ${it.aktorId}.") }
        .onEach { dto -> hendelseService.blittFulgtOpp(dto.aktorId, dto.sluttDato) }
}
