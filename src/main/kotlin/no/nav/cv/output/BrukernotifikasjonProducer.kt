package no.nav.cv.output

import io.micrometer.core.instrument.MeterRegistry
import io.micronaut.context.annotation.Value
import no.nav.brukernotifikasjon.schemas.Done
import no.nav.brukernotifikasjon.schemas.Nokkel
import no.nav.brukernotifikasjon.schemas.Oppgave
import no.nav.brukernotifikasjon.schemas.builders.DoneBuilder
import no.nav.brukernotifikasjon.schemas.builders.NokkelBuilder
import no.nav.brukernotifikasjon.schemas.builders.OppgaveBuilder
import no.nav.cv.notifikasjon.VarselPublisher
import org.slf4j.LoggerFactory
import java.net.URL
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.*
import javax.inject.Singleton

private val systembruker = "srvpambrukernot"
private val grupperingsId = "ARBEIDSPLASSEN"
private val tekst = "Du må oppdatere CV-en og jobbprofilen på arbeidsplassen.no"
private val sikkerhetsnivaa = 3

private val log = LoggerFactory.getLogger(BrukernotifikasjonProducer::class.java)

@Singleton
class BrukernotifikasjonProducer(
        private val meterRegistry: MeterRegistry,
        private val brukernotifikasjonClient: BrukernotifikasjonClient,
        @Value("\${output.melding.link}") private val link: String

) : VarselPublisher {

    override fun publish(eventId: UUID, foedselsnummer: String) {
        log.info("Publiserer varsel med eventId $eventId")

        val nokkel = NokkelBuilder()
                .withSystembruker(systembruker)
                .withEventId(eventId.toString())
                .build()
        val oppgave = OppgaveBuilder()
                .withTidspunkt(LocalDateTime.now())
                .withFodselsnummer(foedselsnummer)
                .withGrupperingsId(grupperingsId)
                .withTekst(tekst)
                .withLink(URL(link))
                .withSikkerhetsnivaa(sikkerhetsnivaa)
                .build()

        brukernotifikasjonClient.publish(nokkel, oppgave)
        meterRegistry.counter("cv.brukernotifikasjon.varsel.opprettet").increment(1.0)
    }

    override fun done(eventId: UUID, foedselsnummer: String) {
        log.info("Publiserer donemelding for eventId $eventId")
        val nokkel = NokkelBuilder()
                .withSystembruker(systembruker)
                .withEventId(eventId.toString())
                .build()
        val done = DoneBuilder()
                .withTidspunkt(LocalDateTime.now())
                .withFodselsnummer(foedselsnummer)
                .withGrupperingsId(grupperingsId)
                .build()

        brukernotifikasjonClient.done(nokkel, done)
        meterRegistry.counter("cv.brukernotifikasjon.varsel.fjernet").increment(1.0)

    }
}