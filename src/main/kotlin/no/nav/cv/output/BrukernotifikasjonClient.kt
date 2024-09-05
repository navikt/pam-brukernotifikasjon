package no.nav.cv.output

import no.nav.brukernotifikasjon.schemas.input.DoneInput
import no.nav.brukernotifikasjon.schemas.input.NokkelInput
import no.nav.brukernotifikasjon.schemas.input.OppgaveInput
import no.nav.cv.infrastructure.kafka.producer.KafkaAivenProducerService
import no.nav.cv.infrastructure.kafka.producer.KafkaAivenProducerService.KafkaProducerDefinition
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class BrukernotifikasjonClient(
    private val kafkaAivenProducerService: KafkaAivenProducerService,
    @Value("\${kafka.aiven.producers.topics.oppgave}") private val oppgaveTopic: String,
    @Value("\${kafka.aiven.producers.topics.done}") private val doneTopic: String
) {
    companion object {
        private val log = LoggerFactory.getLogger(BrukernotifikasjonClient::class.java)
        private const val OPPGAVE_CLIENT_ID = "pam-brukernotifikasjon-oppgave-producer"
        private const val DONE_CLIENT_ID = "pam-brukernotifikasjon-done-producer"
    }

    private val varselProducer: KafkaProducerDefinition<NokkelInput, OppgaveInput> by lazy {
        oppgaveTopic.let {
            log.info("Registrerer varsel-producer for topic: $it")
            kafkaAivenProducerService.notificationProducer(
                topicName = it, clientId = OPPGAVE_CLIENT_ID
            )
        }
    }

    private val doneProducer: KafkaProducerDefinition<NokkelInput, DoneInput> by lazy {
        doneTopic.let {
            log.info("Registrerer done-producer for topic: $it")
            kafkaAivenProducerService.notificationProducer(
                topicName = it, clientId = DONE_CLIENT_ID
            )
        }
    }

    fun varsel(nokkel: NokkelInput, oppgave: OppgaveInput) {
        varselProducer.run {
            //producer.send(ProducerRecord(topicName, nokkel, oppgave))
        }
    }

    fun done(nokkel: NokkelInput, done: DoneInput) {
        doneProducer.run {
            //producer.send(ProducerRecord(topicName, nokkel, done))
        }
    }

}
