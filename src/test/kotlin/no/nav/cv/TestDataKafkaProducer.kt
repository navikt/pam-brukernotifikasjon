package no.nav.cv

import com.ninjasquad.springmockk.MockkBean
import io.mockk.verify
import no.nav.cv.notifikasjon.HendelseService
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.jupiter.api.*
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.utility.DockerImageName
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


@SpringBootTest
@ActiveProfiles("kafka")
@Disabled
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestAKafkaApplication {

    lateinit var oppfolgingsstatusProducer: Producer<String, String>

    companion object {
        private val kafkaContainer: KafkaContainer = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka").withTag("5.4.3"))
    }

    init {
        kafkaContainer.start()
    }

    val aktorId = "1234"


    @MockkBean
    private lateinit var hendelseService: HendelseService

    @BeforeAll
    fun initKafka() {
        val props: Map<String, String> = hashMapOf(Pair("bootstrap.servers", kafkaContainer.bootstrapServers))

        oppfolgingsstatusProducer = KafkaProducer(props,
                StringSerializer(), StringSerializer())
    }

    @AfterAll
    fun tearDownWiremock() {
        kafkaContainer.close()
    }


    @Test
    fun `motta melding om person som er under oppfolging`() {
            val message = oppfolgingsMelding(aktorId, true, ZonedDateTime.now(), ZonedDateTime.now())

            oppfolgingsstatusProducer.send(

                    ProducerRecord(
                            "aapen-fo-endringPaaOppfolgingStatus-v1-q0",
                            "key1",
                            message
                    )
            )
            oppfolgingsstatusProducer.flush()
            verify { hendelseService.harKommetUnderOppfolging(aktorId, any()) }
        }

    @Test
    fun `motta melding om person som ikke er under oppfolging`() {
            val message = oppfolgingsMelding(aktorId, false, ZonedDateTime.now(), ZonedDateTime.now())

            oppfolgingsstatusProducer.send(

                    ProducerRecord(
                            "aapen-fo-endringPaaOppfolgingStatus-v1-q0",
                            "key1",
                            message
                    )
            )
            oppfolgingsstatusProducer.flush()
            verify { hendelseService.ikkeUnderOppfolging(aktorId, any()) }
        }


    fun getProperties(): MutableMap<String, String> {
        // TODO these need to get into springs application-context
        return hashMapOf(Pair("kafka.bootstrap.servers", kafkaContainer.bootstrapServers))
    }


    fun oppfolgingsMelding(aktorId: String, underOppfolging: Boolean, endret: ZonedDateTime, oppfolgingsDato: ZonedDateTime): String {
        return """
                {
                  "aktoerid": "${aktorId}",
                  "veileder": "Z000000",
                  "oppfolging": ${underOppfolging},
                  "nyForVeileder": false,
                  "manuell": false,
                  "endretTimestamp": "${endret.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)}",
                  "startDato": "${oppfolgingsDato.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)}"
                }
            """.trimIndent()
    }

}
