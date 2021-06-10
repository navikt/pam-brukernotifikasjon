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
class OppfolgingAvsluttetProcessor(
        private val hendelseService: HendelseService
) : EventProcessor<String, String> {

    companion object {
        private val log = LoggerFactory.getLogger(OppfolgingAvsluttetProcessor::class.java)
    }

    override suspend fun process(records: ConsumerRecords<String, String>) {
        records.forEach { receiveFinished(it) }
    }

    fun receiveFinished(record: ConsumerRecord<String, String>) = record.value()
            .let { Json.decodeFromString<OppfolgingAvsluttet>(it) }
            .also { log.debug("OppfolgingAvsluttet record received for ${it.aktorId}.") }
            .also { hendelseService.blittFulgtOpp(it.aktorId, it.sluttdato) }
}
