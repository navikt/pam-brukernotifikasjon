package no.nav.cv.output

import io.micronaut.configuration.kafka.annotation.KafkaClient
import io.micronaut.configuration.kafka.annotation.KafkaKey
import io.micronaut.configuration.kafka.annotation.Topic
import no.nav.brukernotifikasjon.schemas.Done
import no.nav.brukernotifikasjon.schemas.Nokkel
import no.nav.brukernotifikasjon.schemas.Oppgave

@KafkaClient()
interface BrukernotifikasjonClient {

    @Topic("\${kafka.topics.producers.ny_oppgave}")
    fun publish(@KafkaKey nokkel: Nokkel, oppgave: Oppgave)

    // TODO : Sjekk hva dette topicet egentlig heter
    @Topic("\${kafka.topics.producers.done}")
    fun done(@KafkaKey nokkel: Nokkel, done: Done)
    
}