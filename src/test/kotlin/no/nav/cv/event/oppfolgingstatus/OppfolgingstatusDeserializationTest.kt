package no.nav.cv.event.oppfolgingstatus

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.cv.notifikasjon.*
import no.nav.cv.output.OutboxService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.stereotype.Repository
import java.time.ZonedDateTime

@DataJpaTest(includeFilters = [ComponentScan.Filter(type = FilterType.ANNOTATION, classes = [Repository::class])])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OppfolgingstatusDeserializationTest {
    @Autowired
    lateinit var statusRepository: StatusRepository

    companion object {
        private val objectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())
        private val AKTOR_ID = "12345678910"
        private val START_DATO = ZonedDateTime.now().withHour(12).withMinute(0).withSecond(0).withNano(0)
        private val SLUTT_DATO = START_DATO.plusHours(2)
    }

    private val outboxServiceMock = Mockito.mock(OutboxService::class.java)
    lateinit var oppfølgingStatusProcessor: OppfolgingstatusProcessor

    @BeforeEach
    fun setup() {
        oppfølgingStatusProcessor = OppfolgingstatusProcessor(Hendelser(statusRepository, outboxServiceMock))
    }

    @Test
    fun `'Oppfølging startet'-melding deserialisering`() {
        val oppfølgingstatus = objectMapper.readValue<OppfølgingstatusDto>(oppfølgingstatusJson(slutt = null))

        assertEquals(AKTOR_ID, oppfølgingstatus.aktorId)
        assertTrue(START_DATO.isEqual(oppfølgingstatus.startDato))
        assertEquals(null, oppfølgingstatus.sluttDato)
    }

    @Test
    fun `'Oppfølging sluttet'-melding deserialisering`() {
        val oppfølgingstatus = objectMapper.readValue<OppfølgingstatusDto>(oppfølgingstatusJson())

        assertEquals(AKTOR_ID, oppfølgingstatus.aktorId)
        assertTrue(START_DATO.isEqual(oppfølgingstatus.startDato))
        assertTrue(SLUTT_DATO.isEqual(oppfølgingstatus.sluttDato))
    }

    @Test
    fun `Håndterer start- og slutt-meldinger riktig`() {
        oppfølgingStatusProcessor.receive(record(0, AKTOR_ID, oppfølgingstatusJson(slutt = null)))
        val startetStatus = statusRepository.finnSiste(AKTOR_ID)

        assertEquals(AKTOR_ID, startetStatus.aktoerId)
        assertEquals(skalVarslesManglerFnrStatus, startetStatus.status)
        assertTrue(START_DATO.isEqual(startetStatus.statusTidspunkt))

        oppfølgingStatusProcessor.receive(record(1, AKTOR_ID, oppfølgingstatusJson()))
        val sluttetStatus = statusRepository.finnSiste(AKTOR_ID)

        assertEquals(AKTOR_ID, sluttetStatus.aktoerId)
        assertEquals(ikkeUnderOppfølgingStatus, sluttetStatus.status)
        assertTrue(SLUTT_DATO.isEqual(sluttetStatus.statusTidspunkt))
    }

    fun oppfølgingstatusJson(
        aktorId: String = AKTOR_ID,
        start: ZonedDateTime? = START_DATO,
        slutt: ZonedDateTime? = SLUTT_DATO
    ) =
        """{
            "aktorId": "$aktorId",
            "startDato": ${start?.let { "\"$start\"" }},
            "sluttDato": ${slutt?.let { "\"$slutt\"" }}
        }""".trimIndent()

    private fun record(offset: Long, aktorId: String, melding: String) =
        ConsumerRecord("TEST-TOPIC", 0, offset, aktorId, melding)
}

