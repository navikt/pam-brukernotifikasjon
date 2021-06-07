package no.nav.cv.output

import no.nav.brukernotifikasjon.schemas.Done
import no.nav.brukernotifikasjon.schemas.Nokkel
import no.nav.brukernotifikasjon.schemas.Oppgave
import no.nav.cv.infrastructure.kafka.Consumer
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
        return KafkaProducer<Nokkel, Done>(props)
    }

    @Bean
    fun oppgaveKafkaProducer(
            @Qualifier("defaultProducerProperties") props: Properties
    ) : KafkaProducer<Nokkel, Oppgave> {
        return KafkaProducer<Nokkel, Oppgave>(props)
    }

    @Bean("producerProperties")
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    fun consumerProperties(
            @Qualifier("defaultConsumerProperties") props: Properties,
    ) : Properties {

        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.canonicalName
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.canonicalName

        props[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
        props[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = false
        props[ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG] = 500000
        props[ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG] = 10000
        props[ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG] = 3000

        props[ConsumerConfig.GROUP_ID_CONFIG] = "pam-brukernotifikasjon-oppfolging-v1"

        return props;
    }



}

