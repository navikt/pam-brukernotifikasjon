package no.nav.cv.event.oppfolgingstatus

import no.nav.cv.infrastructure.kafka.Consumer
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
            @Qualifier("oppfolgingConsumerProperties") props: Properties,
            @Value("\${kafka.topics.consumers.oppfolging_startet}") topic: String,
            eventProcessor: OppfolgingStartetProcessor,
    ) : Consumer<String, String> {

        return Consumer(topic, KafkaConsumer<String, String>(props), eventProcessor)
    }

    @Bean
    fun oppfolgingAvsluttetConsumer(
            @Qualifier("oppfolgingConsumerProperties") props: Properties,
            @Value("\${kafka.topics.consumers.oppfolging_avsluttet}") topic: String,
            eventProcessor: OppfolgingAvsluttetProcessor,
    ) : Consumer<String, String> {

        return Consumer(topic, KafkaConsumer<String, String>(props), eventProcessor)

    }

    @Bean("oppfolgingConsumerProperties")
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
