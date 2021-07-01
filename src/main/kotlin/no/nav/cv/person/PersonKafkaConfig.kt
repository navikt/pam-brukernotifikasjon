package no.nav.cv.person

import io.confluent.kafka.serializers.KafkaAvroDeserializer
import no.nav.cv.infrastructure.kafka.Consumer
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import java.util.*

@Configuration
class PersonConfig {

    private val log = LoggerFactory.getLogger(PersonConfig::class.java)


    @Bean
    fun personConsumer(
            @Qualifier("defaultConsumerProperties") props: Properties,
            @Value("\${kafka.topics.consumers.pdl_id}") topic: String,
            eventProcessor: PersonEndretProcessor,
    ) : Consumer<String, GenericRecord> {

        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.canonicalName
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = KafkaAvroDeserializer::class.java.canonicalName
        props[ConsumerConfig.GROUP_ID_CONFIG] = "pam-brukernotifikasjon-person-v1"
        props[CommonClientConfigs.CLIENT_ID_CONFIG] = "pam-brukernotifikasjon-pdl-id-consumer"

        return Consumer(topic, KafkaConsumer<String, GenericRecord>(props), eventProcessor)
    }

}

