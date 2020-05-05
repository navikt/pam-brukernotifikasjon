package no.nav.cv.event.cv

import kotlinx.coroutines.*
import no.nav.arbeid.cv.avro.Melding
import no.nav.cv.notifikasjon.HendelseService
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.errors.RetriableException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.*
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
class CvConsumer(
        private val hendelseService: HendelseService,
        private val job: Job = Job()
) : CoroutineScope {

    private val log: Logger = LoggerFactory.getLogger(CvConsumer::class.java)

    private val topic = "arbeid-pam-cv-endret-v4-q0"
    val properties = Properties()
    val kafkaConsumer = KafkaConsumer<String, Melding>(properties)

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    suspend fun stopPolling() {
        job.cancelAndJoin()
    }

    fun startPolling() {
        launch {
            kafkaConsumer.use {

                it.subscribe(listOf(topic))

                while(job.isActive) {
                    processBatchOfEvents()
                }

            }
        }
    }

    private suspend fun processBatchOfEvents() {
        try {
            withContext(Dispatchers.IO) {
                kafkaConsumer.poll(Duration.of(1000, ChronoUnit.MILLIS))
            }.takeIf { consumerRecords ->
                consumerRecords.count() > 0
            }?.let {
                // Sjekk om CVen er sett
                //hendelseService.settCv(fnr)
                log.info("Prosesserer CV")
                commitSync()
            }
        } catch (re: RetriableException) {
            log.warn("Polling mot Kafka feilet, prøver igjen senere. Topic: $topic", re)

        } catch (ce: CancellationException) {
            log.info("Denne coroutine-en ble stoppet. ${ce.message}", ce)

        } catch (e: Exception) {
            log.error("Noe uventet feilet, stopper polling. Topic: $topic", e)
            stopPolling()
        }
        TODO("Not yet implemented")
    }


    private suspend fun commitSync() {
        withContext(Dispatchers.IO) {
            kafkaConsumer.commitSync()
        }
    }

}
