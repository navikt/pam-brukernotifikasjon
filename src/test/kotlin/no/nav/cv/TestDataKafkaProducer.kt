package no.nav.cv

import io.micronaut.test.annotation.MicronautTest
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.support.TestPropertyProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.cv.notifikasjon.HendelseService
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.containers.KafkaContainer
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@MicronautTest(environments = ["kafka"])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestAKafkaApplication : TestPropertyProvider {

    @Inject
    lateinit var hendelseService: HendelseService

    lateinit var oppfolgingsstatusProducer: Producer<String, String>

    companion object {
        private val kafkaContainer: KafkaContainer = KafkaContainer()
    }

    init {
        kafkaContainer.start()
    }

    val adIndexerLatch = CountDownLatch(1)
    val aktorId = "1234"


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
            adIndexerLatch.await(20L, TimeUnit.SECONDS)
            verify { hendelseService.kommetUnderOppfolging(aktorId, any()) }
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
            adIndexerLatch.await(20L, TimeUnit.SECONDS)
            verify { hendelseService.erFulgtOpp(aktorId, any()) }
        }


    override fun getProperties(): MutableMap<String, String> {
        return hashMapOf(Pair("kafka.bootstrap.servers", kafkaContainer.bootstrapServers))
    }


    @MockBean(HendelseService::class)
    fun hendelseService(): HendelseService {
        val hendelseService = mockk<HendelseService>(relaxed = true)
        every { hendelseService.erFulgtOpp(any(), any()) } answers {
            adIndexerLatch.countDown()
        }
        every { hendelseService.kommetUnderOppfolging(any(), any()) } answers {
            adIndexerLatch.countDown()
        }
        return hendelseService
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
