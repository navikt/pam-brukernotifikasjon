package no.nav.cv.output

import no.nav.brukernotifikasjon.schemas.Done
import no.nav.brukernotifikasjon.schemas.Nokkel
import no.nav.brukernotifikasjon.schemas.Oppgave
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class BrukernotifikasjonClient(
        @Qualifier("oppgaveKafkaProducer") private val oppgaveKafkaProducer: KafkaProducer<Nokkel, Oppgave>,
        @Qualifier("doneKafkaProducer") private val doneKafkaProducer: KafkaProducer<Nokkel, Done>,
        @Value("\${kafka.topics.producers.ny_oppgave}") private val oppgaveTopic: String,
        @Value("\${kafka.topics.producers.done}") private val doneTopic: String
) {

    fun publish(nokkel: Nokkel, oppgave: Oppgave) {
        throw Exception("Should not reach this point while production of new varsel is disabled")
        oppgaveKafkaProducer.send(ProducerRecord(oppgaveTopic, nokkel, oppgave))
    }

    fun done(nokkel: Nokkel, done: Done) {
        doneKafkaProducer.send(ProducerRecord(doneTopic, nokkel, done))
    }

}
