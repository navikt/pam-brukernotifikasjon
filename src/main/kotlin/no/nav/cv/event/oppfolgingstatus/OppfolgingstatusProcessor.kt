package no.nav.cv.event.oppfolgingstatus

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.cv.infrastructure.kafka.EventProcessor
import no.nav.cv.notifikasjon.HendelseService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.springframework.stereotype.Service
import java.time.ZonedDateTime

@Service
class OppfolgingstatusProcessor(
    private val hendelseService: HendelseService
) : EventProcessor<String, String> {
    companion object {
        val objectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())
    }

    override suspend fun process(records: ConsumerRecords<String, String>) = records.forEach { receive(it) }

    fun receive(record: ConsumerRecord<String, String>) =
        record.value()
            .let { objectMapper.readValue<OppfølgingstatusDto>(it) }
            .also {
                if (it.sluttDato != null) hendelseService.ikkeUnderOppfolging(it.aktorId, it.sluttDato)
                else hendelseService.harKommetUnderOppfolging(it.aktorId, it.startDato)
            }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class OppfølgingstatusDto(
    val aktorId: String,
    val startDato: ZonedDateTime,
    val sluttDato: ZonedDateTime?
)
