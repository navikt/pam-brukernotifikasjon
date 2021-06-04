package no.nav.cv.infrastructure.kafka

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig
import org.apache.kafka.clients.CommonClientConfigs
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

    @Value("\${kafka.bootstrap.servers}")
    private val brokersUrl: String? = null

    @Value("\${kafka.security.protocol}")
    private val securityProtocol: String? = null

    @Value("\${kafka.sasl.jaas.config}")
    private val kafkaSaslJaasConfig: String? = null

    @Value("\${kafka.sasl.mechanism}")
    private val kafkaSaslMechanism: String? = null

    @Value("\${kafka.ssl.truststore.location}")
    private val truststoreLocation: String? = null

    @Value("\${kafka.ssl.truststore.password}")
    private val truststorePassword: String? = null

    @Value("\${kafka.schema.registry.url}")
    private val kafkaSchemaRegistry: String? = null


    @Bean("defaultConsumerProperties")
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    fun kafkaConsumerProperties(): Properties {
        val props = Properties()

        props[CommonClientConfigs.CLIENT_ID_CONFIG] = "pam-kafka-inspector"
        props[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG] = securityProtocol
        props[CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG] = brokersUrl
        props[SaslConfigs.SASL_MECHANISM] = kafkaSaslMechanism
        props[SaslConfigs.SASL_JAAS_CONFIG] = kafkaSaslJaasConfig
        props[SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG] = truststoreLocation
        props[SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG] = truststorePassword
        props[AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG] = kafkaSchemaRegistry

        return props
    }

}
