package no.nav.cv.event.oppfolgingstatus

import io.micronaut.test.annotation.MicronautTest
import junit.framework.Assert.assertEquals
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import no.nav.cv.notifikasjon.StatusRepository
import no.nav.cv.notifikasjon.doneStatus
import no.nav.cv.notifikasjon.nyBrukerStatus
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.jupiter.api.*

import org.testcontainers.containers.KafkaContainer
import org.testcontainers.utility.DockerImageName
import java.time.ZonedDateTime
import javax.inject.Inject
import com.github.dockerjava.api.model.ExposedPort

import com.github.dockerjava.api.model.PortBinding

import com.github.dockerjava.api.command.CreateContainerCmd
import com.github.dockerjava.api.model.Ports
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers


@Disabled   // Due to the fact that tests might not pass when test cointainers are removed from the external repo
            // Also, this doesn't currently work due to not being able to setting bootstrapservers for Kafka.
            // Maybe we can fix this when converting to Spring.
@MicronautTest(environments = ["kafka"])
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class OppfolgingsstatusConsumerTest {

    @Inject
    lateinit var statusRepository: StatusRepository

    lateinit var oppfolgingStartetProducer: Producer<String, String>
    lateinit var oppfolgingAvsluttetProducer: Producer<String, String>

    private val kafkaContainer: KafkaContainer = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:5.4.3"))

    init {
        kafkaContainer.start()
    }

    @BeforeAll
    fun initKafka() {
        val props: Map<String, String> = hashMapOf(Pair("bootstrap.servers", kafkaContainer.bootstrapServers))

        oppfolgingStartetProducer = KafkaProducer(props, StringSerializer(), StringSerializer())
        oppfolgingAvsluttetProducer = KafkaProducer(props, StringSerializer(), StringSerializer())
    }

    @AfterAll
    fun tearDownKafka() {
        kafkaContainer.close()
    }

    @Test
    fun `melding om start av oppfolging`() {

        val aktorId = "123"
        val startedAt = ZonedDateTime.now()

        val message = Json.encodeToString(OppfolgingStartet(aktorId, startedAt))

        oppfolgingStartetProducer.send(ProducerRecord( aktorId, message))
        oppfolgingStartetProducer.flush()

        Thread.sleep(1000)

        val status = statusRepository.finnSiste(aktorId)

        assertEquals(nyBrukerStatus, status.status)
    }

    @Test
    fun `melding om avsluttet oppfolging`() {

        val aktorId = "321"
        val sluttDato = ZonedDateTime.now()

        val message = Json.encodeToString(OppfolgingAvsluttet(aktorId, sluttDato))

        oppfolgingAvsluttetProducer.send(ProducerRecord(aktorId, message))
        oppfolgingAvsluttetProducer.flush()

        Thread.sleep(1000)

        val status = statusRepository.finnSiste(aktorId)

        assertEquals(doneStatus, status.status)
    }

}