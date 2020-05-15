package no.nav.cv.output

import no.nav.brukernotifikasjon.schemas.Done
import no.nav.brukernotifikasjon.schemas.Nokkel
import no.nav.brukernotifikasjon.schemas.Oppgave
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.*


private val LOG: Logger = LoggerFactory.getLogger(BesokCvOppgavePublisher::class.java)
private val topic = "aapen-brukernotifikasjon-nyOppgave-v1"
private val systembruker = "enSystemBruker"
private val grupperingsId = "ARBEIDSPLASSEN"
private val tekst = "På tide å gi CVen litt kjærlighet"
private val link = "https://arbeidsplassen.nav.no/cv/registrering"
private val sikkerhetsnivaa = 3

class BesokCvOppgavePublisher(
        brukernotifikasjonKafkaConfiguration: BrukernotifikasjonKafkaConfiguration
) {
    val props = brukernotifikasjonKafkaConfiguration.producerProps()

    val oppgaveProducer: KafkaProducer<Nokkel, Oppgave> = KafkaProducer(props)
    val doneProducer: KafkaProducer<Nokkel, Done> = KafkaProducer(props)

    fun publish(eventId: UUID, foedselsnummer: String) {
        val nokkel = Nokkel(systembruker, eventId.toString())
        val oppgave = Oppgave(
                Instant.now().toEpochMilli(),
                foedselsnummer,
                grupperingsId,
                tekst,
                link,
                sikkerhetsnivaa
        )

        oppgaveProducer.use {
            it.send(ProducerRecord(topic, nokkel, oppgave)) { metadata, exception ->
                if (exception == null) {
                    // TODO lagre status
                    LOG.info("Record send to Topic: " + metadata.topic() + " Partition: " + metadata.partition() + " Offset: " + metadata.offset());
                } else {
                    // TODO fjerne status
                    LOG.error("Failed to send record to Kafka", exception);
                }
            }
        }
    }

    fun done(eventId: UUID, foedselsnummer: String) {
        val nokkel = Nokkel(systembruker, eventId.toString())
        val done = Done(
                Instant.now().toEpochMilli(),
                foedselsnummer,
                grupperingsId
        )

        doneProducer.use {
            it.send(ProducerRecord(topic, nokkel, done)) { metadata, exception ->
                if (exception == null) {
                    // TODO lagre status
                    LOG.info("Record send to Topic: " + metadata.topic() + " Partition: " + metadata.partition() + " Offset: " + metadata.offset());
                } else {
                    // TODO fjerne status
                    LOG.error("Failed to send record to Kafka", exception);
                }
            }
        }
    }

}