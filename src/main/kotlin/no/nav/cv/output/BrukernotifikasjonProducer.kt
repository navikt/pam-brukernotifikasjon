package no.nav.cv.output

import io.micrometer.core.instrument.MeterRegistry
import no.nav.brukernotifikasjon.schemas.builders.*
import no.nav.cv.notifikasjon.VarselPublisher
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.net.URL
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

private val systembruker = "srvpambrukernot"
private val grupperingsId = "ARBEIDSPLASSEN"
private val tekst = "Du må oppdatere CV-en og jobbønskene dine på arbeidsplassen.no"
private val sikkerhetsnivaa = 3

private val log = LoggerFactory.getLogger(BrukernotifikasjonProducer::class.java)

@Service
class BrukernotifikasjonProducer(
        private val meterRegistry: MeterRegistry,
        private val brukernotifikasjonClient: BrukernotifikasjonClient,
        @Value("\${output.melding.link}") private val link: String

) : VarselPublisher {

    override fun publish(eventId: String, foedselsnummer: String) {
        log.info("Publiserer varsel med eventId $eventId")
        val nokkel = NokkelInputBuilder()
            .withEventId(eventId)
            .withGrupperingsId(grupperingsId)
            .withFodselsnummer(foedselsnummer)
            .withNamespace("hei") // TODO - TEAMPAM?
            .withAppnavn("du") // TODO - BRUKERNOTIFIKASJON?
            .build()

        val oppgave = OppgaveInputBuilder()
            .withTidspunkt(LocalDateTime.now(ZoneOffset.UTC))
            //.withSynligFremTil()
            .withTekst(tekst)
            .withLink(URL(link))
            .withSikkerhetsnivaa(sikkerhetsnivaa)
            .build()


        brukernotifikasjonClient.publish(nokkel, oppgave)
        meterRegistry.counter("cv.brukernotifikasjon.varsel.opprettet").increment(1.0)
    }

    override fun done(eventId: String, foedselsnummer: String) {
        log.info("Publiserer donemelding for eventId $eventId")
        val nokkel = NokkelInputBuilder()
            .withEventId(eventId)
            .withGrupperingsId(grupperingsId)
            .withFodselsnummer(foedselsnummer)
            .withNamespace("hei") // TODO - TEAMPAM?
            .withAppnavn("du") // TODO - BRUKERNOTIFIKASJON?
            .build()

        val done = DoneInputBuilder()
            .withTidspunkt(LocalDateTime.now(ZoneOffset.UTC))

        brukernotifikasjonClient.done(nokkel, done)
        meterRegistry.counter("cv.brukernotifikasjon.varsel.fjernet").increment(1.0)

    }
}
