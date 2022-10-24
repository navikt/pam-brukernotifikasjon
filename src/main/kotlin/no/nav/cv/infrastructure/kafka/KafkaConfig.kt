package no.nav.cv.infrastructure.kafka

import io.micrometer.core.instrument.MeterRegistry
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.config.SslConfigs
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import java.util.*

@Configuration
class KafkaConfig {
    @Value("\${kafka.aiven.brokers}")
    private val brokersUrl: String? = null

    @Value("\${kafka.aiven.securityProtocol}")
    private val securityProtocol: String? = null

    @Value("\${kafka.aiven.keystorePath}")
    private val keystorePath: String? = null

    @Value("\${kafka.aiven.truststorePath}")
    private val truststorePath: String? = null

    @Value("\${kafka.aiven.credstorePassword}")
    private val credstorePassword: String? = null


    @Bean("defaultConsumerProperties")
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    fun defaultConsumerProperties(): Properties {
        val props = defaultConsumerClusterProperties()

        props[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
        props[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = false
        props[ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG] = 500000
        props[ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG] = 10000
        props[ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG] = 3000

        return props
    }

    private fun defaultConsumerClusterProperties(): Properties {
        val props = Properties()

        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = brokersUrl
        props[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG] = securityProtocol
        props[SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG] = "JKS"
        props[SslConfigs.SSL_KEY_PASSWORD_CONFIG] = credstorePassword
        props[SslConfigs.SSL_KEYSTORE_TYPE_CONFIG] = "PKCS12"

        System.getenv("KAFKA_KEYSTORE_PATH")?.let {
            props[SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG] = keystorePath
            props[SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG] = credstorePassword
        }

        System.getenv("KAFKA_TRUSTSTORE_PATH")?.let {
            props[SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG] = truststorePath
            props[SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG] = credstorePassword
            props[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG] = "SSL"
        }

        return props
    }

    @Bean
    fun consumerStatus(consumers: List<Consumer<out Any, out Any>>) = ConsumerStatusHandler(consumers)

    @Bean
    fun consumerMetrics(consumers: List<Consumer<out Any, out Any>>, meterRegistry: MeterRegistry) =
        ConsumerStatusMetrics(consumers, meterRegistry)
}
