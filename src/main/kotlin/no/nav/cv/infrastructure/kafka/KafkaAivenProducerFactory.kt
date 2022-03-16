package no.nav.cv.infrastructure.kafka

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.*
import io.confluent.kafka.serializers.KafkaAvroSerializer
import org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerConfig.*
import org.apache.kafka.common.config.SslConfigs
import java.util.*

object KafkaAivenProducerFactory {
    fun <K, V> create(
        clientId: String,
        brokersUrl: String,
        securityProtocolConfig: String,
        kafkaSchemaRegistry: String,
        kafkaSchemaUsername: String? = null,
        kafkaSchemaPassword: String? = null,
        batchSizeConfig: Int = 65536,
        lingerMsConfig: Int = 5000,
        keystorePath: String? = null,
        truststorePath: String? = null,
        credstorePassword: String? = null,
    ): Producer<K, V> {

        val props = Properties()
        props[SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG] = credstorePassword
        props[SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG] = credstorePassword
        props[SslConfigs.SSL_KEY_PASSWORD_CONFIG] = credstorePassword
        props[SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG] = "JKS"
        props[SslConfigs.SSL_KEYSTORE_TYPE_CONFIG] = "PKCS12"

        props[SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG] = truststorePath
        props[SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG] = keystorePath

        props[BASIC_AUTH_CREDENTIALS_SOURCE] = "USER_INFO"
        props[USER_INFO_CONFIG] = "${kafkaSchemaUsername}:${kafkaSchemaPassword}"

        props[CLIENT_ID_CONFIG] = clientId
        props[SECURITY_PROTOCOL_CONFIG] = securityProtocolConfig
        props[BOOTSTRAP_SERVERS_CONFIG] = brokersUrl
        props[KEY_SERIALIZER_CLASS_CONFIG] = KafkaAvroSerializer::class.java.canonicalName
        props[VALUE_SERIALIZER_CLASS_CONFIG] = KafkaAvroSerializer::class.java.canonicalName
        props[SCHEMA_REGISTRY_URL_CONFIG] = kafkaSchemaRegistry
        props[LINGER_MS_CONFIG] = lingerMsConfig
        props[BATCH_SIZE_CONFIG] = batchSizeConfig
        props[ACKS_CONFIG] = "all"

        return KafkaProducer(props)
    }
}