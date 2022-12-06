package no.nav.cv.event.cv

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.cv.notifikasjon.Hendelser
import no.nav.cv.notifikasjon.Status
import no.nav.cv.notifikasjon.StatusRepository
import no.nav.cv.output.OutboxService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.time.ZoneId
import java.time.ZonedDateTime

class CvDeserializationTest {
    companion object {
        private val objectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())
        private val AKTOR_ID = "12345678910"
        private val MELDINGSTYPE = CvMeldingstype.ENDRE
        private val UPDATED_BY = UpdatedByType.PERSONBRUKER
        private val UPDATED_AT = ZonedDateTime.of(2022, 10, 10, 12, 0, 0, 0, ZoneId.systemDefault())

        private val outboxServiceMock = mock(OutboxService::class.java)
        private val statusRepositoryMock = mock(StatusRepository::class.java)
        private val statusCaptor = com.nhaarman.mockitokotlin2.argumentCaptor<Status>()

        private val hendelseService = Hendelser(statusRepositoryMock, outboxServiceMock)
        private val cvEndretProcessor = CvEndretProcessor(hendelseService)
    }

    @Test
    fun `deserialiserer cvEndret json riktig`() {
        val cv = objectMapper.readValue<CvDto>(cvJson())
        assertEquals(AKTOR_ID, cv.aktorId)
        assertEquals(CvMeldingstype.ENDRE, cv.meldingstype)
        assertEquals(UpdatedByType.PERSONBRUKER, cv.updatedBy)
        assertTrue(UPDATED_AT.isEqual(cv.sistEndret()))
    }

    @Test
    fun `cvEndretProsessor håndterer json-payload riktig`() {
        `when`(statusRepositoryMock.finnSiste(AKTOR_ID)).thenReturn(Status.nySession(AKTOR_ID))

        cvEndretProcessor.receive(record(0, AKTOR_ID, cvJson()))

        verify(statusRepositoryMock, times(1)).lagreNyStatus(statusCaptor.capture())

        val lagretStatus = statusCaptor.allValues[0]

        assertEquals("cvOppdatert", lagretStatus.status)
        assertTrue(UPDATED_AT.isEqual(lagretStatus.statusTidspunkt))
    }


    @Test
    fun `cvEndretProcessor ignorerer CV-events som er slettemelding eller ikke er gjort av bruker`() {
        `when`(statusRepositoryMock.finnSiste(AKTOR_ID)).thenReturn(Status.nySession(AKTOR_ID))

        cvEndretProcessor.receive(record(0, AKTOR_ID, cvJson(meldingstype = CvMeldingstype.SLETT.name)))
        cvEndretProcessor.receive(record(1, AKTOR_ID, cvJson(updatedBy = UpdatedByType.VEILEDER.name)))

        verify(statusRepositoryMock, times(0))
            .lagreNyStatus(statusCaptor.capture())

        assertTrue(statusCaptor.allValues.isEmpty())
    }


    private fun record(offset: Long, aktorId: String, melding: String) =
        ConsumerRecord("test-topic", 0, offset, aktorId, melding)

    private fun cvJson(
        aktorId: String = AKTOR_ID,
        meldingstype: String = MELDINGSTYPE.name,
        updatedBy: String = UPDATED_BY.name,
        updatedAt: String = UPDATED_AT.toString()
    ) = """
        {
          "aktorId": "$aktorId",
          "fodselsnummer": "12345678910",
          "meldingstype": "$meldingstype",
          "cv": {
            "uuid": "273655e0-06cd-4d56-87e5-4cab9b7023c6",
            "hasCar": false,
            "summary": "as",
            "draft": null,
            "otherExperience": [],
            "converted": false,
            "convertedTimestamp": null,
            "createdAt": "2022-05-10T09:48:45.420114+02:00",
            "updatedAt": "$updatedAt"
          },
          "personalia": {},
          "jobWishes": {},
          "updatedBy": "$updatedBy"
        }
    """.trimIndent()
}
