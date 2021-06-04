package no.nav.cv.event.cv

import io.confluent.kafka.serializers.KafkaAvroDeserializer
import no.nav.cv.event.oppfolgingstatus.KafkaConsumerStartupService
import no.nav.cv.infrastructure.kafka.Consumer
import org.apache.avro.generic.GenericRecord
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
    fun cvEndretConsumer(
            @Qualifier("cvConsumerProperties") props: Properties,
            @Value("\${kafka.topics.consumers.cv_endret}") topic: String,
            eventProcessor: CvEndretProcessor,
    ) : Consumer<String, GenericRecord> {

        return Consumer(topic, KafkaConsumer<String, GenericRecord>(props), eventProcessor)
    }


    @Bean("cvConsumerProperties")
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    fun consumerProperties(
            @Qualifier("defaultConsumerProperties") props: Properties,
    ) : Properties {

        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.canonicalName
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = KafkaAvroDeserializer::class.java.canonicalName

        props[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
        props[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = false
        props[ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG] = 500000
        props[ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG] = 10000
        props[ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG] = 3000

        props[ConsumerConfig.GROUP_ID_CONFIG] = "pam-brukernotifikasjon-cv-v4"

        return props;
    }

    @Bean
    fun kafkaCvConsumerStartupService(
            @Qualifier("cvEndretConsumer") cvEndretConsumer: Consumer<String, GenericRecord>
    ): KafkaCvConsumerStartupService {
        return KafkaCvConsumerStartupService(listOf(cvEndretConsumer))
    }


}


class KafkaCvConsumerStartupService(
        private val consumers: List<Consumer<String, GenericRecord>>
) {

    @EventListener(ApplicationReadyEvent::class)
    fun startUp() {
        consumers.forEach { it.startPolling() }
    }

}
