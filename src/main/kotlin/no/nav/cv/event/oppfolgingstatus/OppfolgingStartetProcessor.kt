package no.nav.cv.event.oppfolgingstatus

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import no.nav.cv.infrastructure.kafka.EventProcessor
import no.nav.cv.notifikasjon.HendelseService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class OppfolgingStartetProcessor(
        private val hendelseService: HendelseService
) : EventProcessor<String, String> {

    companion object {
        private val log = LoggerFactory.getLogger(OppfolgingStartetProcessor::class.java)
    }

    override suspend fun process(records: ConsumerRecords<String, String>) {
        records.forEach { receiveBegun(it) }
    }

    fun receiveBegun(record: ConsumerRecord<String, String>) = record.value()
            .also { json -> log.debug("json: \"$json\"") }
            .map { json -> Json.decodeFromString<OppfolgingStartet>(json.toString()) }
            .onEach { log.debug("OppfolgingStartet record received for ${it.aktorId}.") }
            .onEach { dto -> hendelseService.kommetUnderOppfolging(dto.aktorId, dto.oppfolgingStartet) }
}
