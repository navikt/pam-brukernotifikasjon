package no.nav.cv.event.cv

import no.nav.cv.infrastructure.kafka.Consumer
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import java.util.*

@Configuration
class CvKafkaConfig {
    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    fun cvEndretConsumer(
        @Qualifier("defaultConsumerPropertiesAiven") props: Properties,
        @Value("\${kafka.aiven.consumers.topics.cv_endret}") topic: String,
        eventProcessor: CvEndretProcessor,
    ): Consumer<String, String> {
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.canonicalName
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.canonicalName
        props[ConsumerConfig.GROUP_ID_CONFIG] = "pam-brukernotifikasjon-cv-v6"
        props[CommonClientConfigs.CLIENT_ID_CONFIG] = "pam-brukernotifikasjon-cv-endret-consumer"

        return Consumer(topic, KafkaConsumer<String, String>(props), eventProcessor)
    }
}
