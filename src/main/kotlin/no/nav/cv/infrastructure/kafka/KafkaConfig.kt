package no.nav.cv.infrastructure.kafka

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig
import io.micrometer.core.instrument.MeterRegistry
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.config.SslConfigs
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

    @Value("\${kafka.aiven.brokers}")
    private val brokersUrlAiven: String? = null

    @Value("\${kafka.aiven.securityProtocol}")
    private val securityProtocolAiven: String? = null

    @Value("\${kafka.aiven.keystorePath}")
    private val keystorePathAiven: String? = null

    @Value("\${kafka.aiven.truststorePath}")
    private val truststorePathAiven: String? = null

    @Value("\${kafka.aiven.credstorePassword}")
    private val credstorePasswordAiven: String? = null

    private fun defaultConsumerProperties(): Properties {
        val props = Properties()

        props[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
        props[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = false
        props[ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG] = 500000
        props[ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG] = 10000
        props[ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG] = 3000

        return props
    }

    @Bean("defaultConsumerPropertiesOnPrem")
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    fun defaultConsumerPropertiesOnPrem(): Properties {
        val props = defaultConsumerProperties()

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

    @Bean("defaultConsumerPropertiesAiven")
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    fun defaultConsumerPropertiesAiven(): Properties {
        val props = defaultConsumerProperties()

        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = brokersUrlAiven
        props[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG] = securityProtocolAiven
        props[SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG] = "JKS"
        props[SslConfigs.SSL_KEY_PASSWORD_CONFIG] = credstorePasswordAiven
        props[SslConfigs.SSL_KEYSTORE_TYPE_CONFIG] = "PKCS12"

        System.getenv("KAFKA_KEYSTORE_PATH")?.let {
            props[SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG] = keystorePathAiven
            props[SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG] = credstorePasswordAiven
        }

        System.getenv("KAFKA_TRUSTSTORE_PATH")?.let {
            props[SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG] = truststorePathAiven
            props[SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG] = credstorePasswordAiven
            props[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG] = "SSL"
        }

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
