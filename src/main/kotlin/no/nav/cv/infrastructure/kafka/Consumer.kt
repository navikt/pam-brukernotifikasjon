package no.nav.cv.infrastructure.kafka

import kotlinx.coroutines.*
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.errors.RetriableException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.temporal.ChronoUnit
import kotlin.coroutines.CoroutineContext

class Consumer<K, V>(
        val topic: String,
        val kafkaConsumer: KafkaConsumer<K, V>,
        val eventProcessor: EventProcessor<K, V>,
        val job: Job = Job()
) : CoroutineScope {

    private val log: Logger = LoggerFactory.getLogger(Consumer::class.java)

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    suspend fun stopPolling() {
        job.cancelAndJoin()
    }

    fun isCompleted(): Boolean {
        return job.isCompleted
    }

    fun isStopped(): Boolean {
        return !job.isActive
    }

    fun startPolling() {
        val handler = CoroutineExceptionHandler { _, exception ->
            log.error("CoroutineExceptionHandler got $exception")
        }

        launch(handler) {
            kafkaConsumer.use { consumer ->
                consumer.subscribe(listOf(topic))

                while (job.isActive) {
                    pollForAndRelayBatchOfEvents()
                }
            }
        }
    }

    private suspend fun pollForAndRelayBatchOfEvents() = withContext(Dispatchers.IO) {
        try {
            val records = kafkaConsumer.poll(Duration.of(100, ChronoUnit.MILLIS))
            if (records.containsEvents()) {
                eventProcessor.process(records)
                kafkaConsumer.commitSync()
            }
        }  catch (re: RetriableException) {
            log.warn("Polling mot Kafka feilet, prøver igjen senere. Topic: $topic", re)

        } catch (ce: CancellationException) {
            log.info("Denne coroutine-en ble stoppet. ${ce.message}", ce)

        } catch (e: Exception) {
            log.error("Noe uventet feilet, stopper polling. Topic: $topic", e)
            stopPolling()
        }
    }

    fun ConsumerRecords<K, V>.containsEvents() = count() > 0

    private suspend fun rollbackOffset() {
        withContext(Dispatchers.IO) {
            kafkaConsumer.rollbackToLastCommitted()
        }
    }

    fun <K, V> KafkaConsumer<K, V>.rollbackToLastCommitted() {
        committed(assignment()).forEach { (partition, metadata) ->
            seek(partition, metadata.offset())
        }
    }

}
