package no.nav.cv.infrastructure.kafka

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig
import io.confluent.kafka.serializers.KafkaAvroSerializer
import io.micrometer.core.instrument.MeterRegistry
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import java.util.*

@Configuration
class KafkaConfig {

    @Value("\${kafka.properties.bootstrap-servers}")
    private val brokersUrl: String? = null

    @Value("\${kafka.sasl.jaas.config}")
    private val kafkaSaslJaasConfig: String? = null

    @Value("\${kafka.ssl.truststore.location}")
    private val truststoreLocation: String? = null

    @Value("\${kafka.ssl.truststore.password}")
    private val truststorePassword: String? = null

    @Value("\${kafka.properties.schema.registry.url}")
    private val kafkaSchemaRegistry: String? = null


    @Bean("defaultConsumerProperties")
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    fun kafkaConsumerProperties(): Properties {
        val props = kafkaClusterProperties()

        props[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
        props[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = false
        props[ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG] = 500000
        props[ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG] = 10000
        props[ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG] = 3000

        return props
    }

    private fun kafkaClusterProperties(): Properties {

        val props = Properties()

        props[CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG] = brokersUrl
        props[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG] = "PLAINTEXT"
        props[SaslConfigs.SASL_MECHANISM] = "PLAIN"
        props[SaslConfigs.SASL_JAAS_CONFIG] = kafkaSaslJaasConfig
        System.getenv("NAV_TRUSTSTORE_PATH")?.let {
            props[SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG] = truststoreLocation
            props[SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG] = truststorePassword
            props[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG] = "SASL_SSL"

        }
        props[AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG] = kafkaSchemaRegistry

        return props
    }

    @Bean
    fun consumerStatus(
        consumers: List<Consumer<out Any, out Any>>
    ): ConsumerStatusHandler {
        return ConsumerStatusHandler(consumers)
    }

    @Bean
    fun consumerMetrics(
        consumers: List<Consumer<out Any, out Any>>,
        meterRegistry: MeterRegistry
    ): ConsumerStatusMetrics {
        return ConsumerStatusMetrics(consumers, meterRegistry)
    }

}