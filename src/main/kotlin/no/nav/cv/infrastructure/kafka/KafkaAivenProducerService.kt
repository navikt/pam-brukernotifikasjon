package no.nav.cv.infrastructure.kafka

import org.apache.kafka.clients.producer.Producer
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class KafkaAivenProducerService (
    @Value("\${kafka.aiven.brokers}")
    private val brokersUrlAiven: String,

    @Value("\${kafka.aiven.securityProtocol}")
    private val securityProtocolConfigAiven: String,

    @Value("\${kafka.aiven.schemaRegistry}")
    private val kafkaSchemaRegistryAiven: String,

    @Value("\${kafka.aiven.schemaUsername}")
    private val kafkaSchemaUsernameAiven: String,

    @Value("\${kafka.aiven.schemaPassword}")
    private val kafkaSchemaPasswordAiven: String,

    @Value("\${kafka.aiven.keystorePath}")
    private val keystorePath: String,

    @Value("\${kafka.aiven.truststorePath}")
    private val truststorePath: String,
    
    @Value("\${kafka.aiven.credstorePassword}")
    private val credstorePassword: String,
) {

    data class KafkaProducerDefinition<K, V>(
        val topicName: String,
        val producer: Producer<K, V>
    )

    fun <K, V> notificationProducer(
        clientId: String,
        topicName: String,
    ): KafkaProducerDefinition<K, V> =
            KafkaProducerDefinition(
                topicName = topicName,
                producer = KafkaAivenProducerFactory.create(
                    clientId = clientId,
                    brokersUrl = brokersUrlAiven,
                    securityProtocolConfig = securityProtocolConfigAiven,
                    kafkaSchemaRegistry = kafkaSchemaRegistryAiven,
                    kafkaSchemaUsername = kafkaSchemaUsernameAiven,
                    kafkaSchemaPassword = kafkaSchemaPasswordAiven,
                    keystorePath = keystorePath,
                    truststorePath = truststorePath,
                    credstorePassword = credstorePassword,
                )
            )
}

