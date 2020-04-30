package no.nav.cv.output

import no.nav.brukernotifikasjon.schemas.Done
import no.nav.brukernotifikasjon.schemas.Nokkel
import no.nav.brukernotifikasjon.schemas.Oppgave
import no.nav.cv.notifikasjon.VarselPublisher
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.*
import javax.inject.Singleton


private val LOG: Logger = LoggerFactory.getLogger(BesokCvOppgavePublisher::class.java)
private val topic = "aapen-brukernotifikasjon-nyOppgave-v1"
private val systembruker = "enSystemBruker"
private val grupperingsId = "ARBEIDSPLASSEN"
private val tekst = "På tide å gi CVen litt kjærlighet"
private val link = "https://arbeidsplassen.nav.no/cv/registrering"
private val sikkerhetsnivaa = 4

@Singleton
class BesokCvOppgavePublisher(
        brukernotifikasjonKafkaConfiguration: BrukernotifikasjonKafkaConfiguration
) : VarselPublisher {
    val props = brukernotifikasjonKafkaConfiguration.producerProps()

    init {
        /*
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:29092"
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = KafkaAvroSerializer::class.java.getName()
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = KafkaAvroSerializer::class.java.getName()
        props[AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG] = "http://localhost:8081/"

        // close producer on shutdown
        // close producer on shutdown
        Runtime.getRuntime().addShutdownHook(Thread(Runnable {
            LOG.info("closing oppgaveProducer...")
            oppgaveProducer.flush()
            oppgaveProducer.close()
            LOG.info("closing doneProducer...")
            doneProducer.flush()
            doneProducer.close()
            LOG.info("done!")
        }))
        */
    }

    val oppgaveProducer: KafkaProducer<Nokkel, Oppgave> = KafkaProducer(props)
    val doneProducer: KafkaProducer<Nokkel, Done> = KafkaProducer(props)

    override fun publish(eventId: UUID, foedselsnummer: String) {
        val nokkel = Nokkel(systembruker, eventId.toString())
        val oppgave = Oppgave(
                Instant.now().toEpochMilli(),
                foedselsnummer,
                grupperingsId,
                tekst,
                link,
                sikkerhetsnivaa
        )
        val record = ProducerRecord(topic, nokkel, oppgave)

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

    override fun done(eventId: UUID, foedselsnummer: String) {
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