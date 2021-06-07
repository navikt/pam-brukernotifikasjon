package no.nav.cv.output

import no.nav.brukernotifikasjon.schemas.Done
import no.nav.brukernotifikasjon.schemas.Nokkel
import no.nav.brukernotifikasjon.schemas.Oppgave
import no.nav.cv.infrastructure.kafka.Consumer
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import org.springframework.context.event.EventListener
import java.util.*

@Configuration
class BrukernotifikasjonKafkaConfig {

    private val log = LoggerFactory.getLogger(BrukernotifikasjonKafkaConfig::class.java)


    @Bean
    fun doneKafkaProducer(
            @Qualifier("defaultProducerProperties") props: Properties
    ) : KafkaProducer<Nokkel, Done> {
        props[CommonClientConfigs.CLIENT_ID_CONFIG] = "pam-brukernotifikasjon-done-producer"
        return KafkaProducer<Nokkel, Done>(props)
    }

    @Bean
    fun oppgaveKafkaProducer(
            @Qualifier("defaultProducerProperties") props: Properties
    ) : KafkaProducer<Nokkel, Oppgave> {

        props[CommonClientConfigs.CLIENT_ID_CONFIG] = "pam-brukernotifikasjon-oppgave-producer"
        return KafkaProducer<Nokkel, Oppgave>(props)
    }


}

