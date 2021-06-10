package no.nav.cv.infrastructure.kafka

import org.apache.kafka.clients.consumer.ConsumerRecords

interface EventProcessor<K, V> {
    suspend fun process(records: ConsumerRecords<K, V>)
}
