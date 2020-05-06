package no.nav.cv.output

import io.micronaut.configuration.kafka.annotation.KafkaClient
import io.micronaut.configuration.kafka.annotation.KafkaKey
import io.micronaut.configuration.kafka.annotation.Topic
import no.nav.brukernotifikasjon.schemas.Done
import no.nav.brukernotifikasjon.schemas.Nokkel
import no.nav.brukernotifikasjon.schemas.Oppgave

@KafkaClient()
interface BrukernotifikasjonClient {

    @Topic("aapen-brukernotifikasjon-nyOppgave-v1")
    fun publish(@KafkaKey nokkel: Nokkel, oppgave: Oppgave)

    @Topic("aapen-brukernotifikasjon-nyOppgave-v1")
    fun done(@KafkaKey nokkel: Nokkel, done: Done)
    
}