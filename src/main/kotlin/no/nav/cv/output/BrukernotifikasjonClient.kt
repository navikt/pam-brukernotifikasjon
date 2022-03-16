package no.nav.cv.output

import no.nav.brukernotifikasjon.schemas.input.DoneInput
import no.nav.brukernotifikasjon.schemas.input.NokkelInput
import no.nav.brukernotifikasjon.schemas.input.OppgaveInput
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class BrukernotifikasjonClient(
        @Qualifier("oppgaveKafkaProducer") private val oppgaveKafkaProducer: KafkaProducer<NokkelInput, OppgaveInput>,
        @Qualifier("doneKafkaProducer") private val doneKafkaProducer: KafkaProducer<NokkelInput, DoneInput>,
        @Value("\${kafka.topics.producers.ny_oppgave}") private val oppgaveTopic: String,
        @Value("\${kafka.topics.producers.done}") private val doneTopic: String
) {

    fun publish(nokkel: NokkelInput, oppgave: OppgaveInput) {
        oppgaveKafkaProducer.send(ProducerRecord(oppgaveTopic, nokkel, oppgave))
    }

    fun done(nokkel: NokkelInput, done: DoneInput) {
        doneKafkaProducer.send(ProducerRecord(doneTopic, nokkel, done))
    }

}
