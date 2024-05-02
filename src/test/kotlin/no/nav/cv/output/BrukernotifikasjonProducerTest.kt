package no.nav.cv.output

import io.micrometer.core.instrument.MeterRegistry
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.brukernotifikasjon.schemas.input.DoneInput
import no.nav.brukernotifikasjon.schemas.input.NokkelInput
import no.nav.brukernotifikasjon.schemas.input.OppgaveInput
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

class BrukernotifikasjonProducerTest {

    private val meterRegistry = mockk<MeterRegistry>(relaxed = true)
    private val brukernotifikasjonClient = mockk<BrukernotifikasjonClient>()
    private val link: String = "http://test.link"
    private val brukernotifikasjonProducer = BrukernotifikasjonProducer(meterRegistry, brukernotifikasjonClient, link)

    @Test
    fun `Skal produsere oppgave`() {
        val expectedEventId = UUID.randomUUID().toString()
        val expectedFødselsnummer = "1".repeat(11)
        val expectedSynligFramTil = LocalDateTime.now(ZoneOffset.UTC).toLocalDate().plusDays(30)

        val nøkkelSlot = slot<NokkelInput>()
        val oppgaveSlot = slot<OppgaveInput>()
        every { brukernotifikasjonClient.varsel(capture(nøkkelSlot), capture(oppgaveSlot)) } answers { }

        brukernotifikasjonProducer.publish(expectedEventId, expectedFødselsnummer)

        verify(exactly = 1) { brukernotifikasjonClient.varsel(any(), any()) }

        val capturedNokkel = nøkkelSlot.captured
        assertEquals(expectedEventId, capturedNokkel.eventId)
        assertEquals(expectedFødselsnummer, capturedNokkel.fodselsnummer)
        assertEquals("teampam", capturedNokkel.namespace)
        assertEquals("pam-brukernotifikasjon", capturedNokkel.appnavn)

        val capturedOppgave = oppgaveSlot.captured
        assertNotNull(capturedOppgave.tidspunkt)
        val synligFramTilAsLocalDate =
            Instant.ofEpochMilli(capturedOppgave.synligFremTil).atZone(ZoneOffset.UTC).toLocalDate()
        assertEquals(expectedSynligFramTil, synligFramTilAsLocalDate)
        assertEquals("Du må oppdatere CV-en og jobbønskene dine på arbeidsplassen.no", capturedOppgave.tekst)
        assertEquals(link, capturedOppgave.link)
        assertEquals(3, capturedOppgave.sikkerhetsnivaa)
    }

    @Test
    fun `Skal produsere done-event`() {
        val expectedEventId = UUID.randomUUID().toString()
        val expectedFødselsnummer = "2".repeat(11)

        val nøkkelSlot = slot<NokkelInput>()
        val doneSlot = slot<DoneInput>()
        every { brukernotifikasjonClient.done(capture(nøkkelSlot), capture(doneSlot)) } answers { }

        brukernotifikasjonProducer.done(expectedEventId, expectedFødselsnummer)

        verify(exactly = 1) { brukernotifikasjonClient.done(any(), any()) }

        val capturedNokkel = nøkkelSlot.captured
        assertEquals(expectedEventId, capturedNokkel.eventId)
        assertEquals(expectedFødselsnummer, capturedNokkel.fodselsnummer)
        assertEquals("teampam", capturedNokkel.namespace)
        assertEquals("pam-brukernotifikasjon", capturedNokkel.appnavn)

        val capturedDone = doneSlot.captured
        assertNotNull(capturedDone.tidspunkt)
    }

}
