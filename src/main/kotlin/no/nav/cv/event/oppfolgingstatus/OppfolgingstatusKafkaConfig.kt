package no.nav.cv.event.oppfolgingstatus

import no.nav.cv.infrastructure.kafka.Consumer
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*

@Configuration
class OppfolgingstatusKafkaConfig {
    @Bean
    fun oppfolgingStatusConsumer(
        @Qualifier("defaultConsumerProperties") props: Properties,
        @Value("\${kafka.aiven.consumers.topics.oppfolging_status}") topic: String,
        eventProcessor: OppfolgingstatusProcessor,
    ): Consumer<String, String> {

        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.canonicalName
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.canonicalName
        props[ConsumerConfig.GROUP_ID_CONFIG] = "pam-brukernotifikasjon-oppfolging-status-v1"
        props[CommonClientConfigs.CLIENT_ID_CONFIG] = "pam-brukernotifikasjon-oppfolging-status-consumer"

        return Consumer(topic, KafkaConsumer<String, String>(props), eventProcessor)
    }
}
