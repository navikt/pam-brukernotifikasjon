package no.nav.cv.event.oppfolgingstatus

import no.nav.cv.infrastructure.kafka.Consumer
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import org.springframework.context.event.EventListener
import java.util.*

@Configuration
class OppfolgingstatusKafkaConfig {

    private val log = LoggerFactory.getLogger(OppfolgingstatusKafkaConfig::class.java)


    @Bean
    fun oppfolgingStartetConsumer(
            @Qualifier("defaultConsumerProperties") props: Properties,
            @Value("\${kafka.topics.consumers.oppfolging_startet}") topic: String,
            eventProcessor: OppfolgingStartetProcessor,
    ) : Consumer<String, String> {

        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.canonicalName
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.canonicalName
        props[ConsumerConfig.GROUP_ID_CONFIG] = "pam-brukernotifikasjon-oppfolging-startet-v2"
        props[CommonClientConfigs.CLIENT_ID_CONFIG] = "pam-brukernotifikasjon-oppfolging-startet-consumer"

        return Consumer(topic, KafkaConsumer<String, String>(props), eventProcessor)
    }

    @Bean
    fun oppfolgingAvsluttetConsumer(
            @Qualifier("defaultConsumerProperties") props: Properties,
            @Value("\${kafka.topics.consumers.oppfolging_avsluttet}") topic: String,
            eventProcessor: OppfolgingAvsluttetProcessor,
    ) : Consumer<String, String> {

        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.canonicalName
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.canonicalName
        props[ConsumerConfig.GROUP_ID_CONFIG] = "pam-brukernotifikasjon-oppfolging-avsluttet-v2"
        props[CommonClientConfigs.CLIENT_ID_CONFIG] = "pam-brukernotifikasjon-oppfolging-avsluttet-consumer"

        return Consumer(topic, KafkaConsumer<String, String>(props), eventProcessor)

    }

    @Bean
    fun kafkaConsumerStartupService(
            @Qualifier("oppfolgingStartetConsumer") oppfolgingStartetConsumer: Consumer<String, String>,
            @Qualifier("oppfolgingAvsluttetConsumer") oppfolgingAvsluttetConsumer: Consumer<String, String>,
    ): KafkaConsumerStartupService {
        return KafkaConsumerStartupService(listOf(oppfolgingStartetConsumer, oppfolgingAvsluttetConsumer))
    }


}


class KafkaConsumerStartupService(
        private val consumers: List<Consumer<String, String>>
) {

    @EventListener(ApplicationReadyEvent::class)
    fun startUp() {
        consumers.forEach { it.startPolling() }
    }

}
