package no.nav.cv.output

import io.micronaut.context.annotation.Value
import no.nav.brukernotifikasjon.schemas.Done
import no.nav.brukernotifikasjon.schemas.Nokkel
import no.nav.brukernotifikasjon.schemas.Oppgave
import no.nav.cv.notifikasjon.VarselPublisher
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.*
import javax.inject.Singleton

private val systembruker = "srvpambrukernot"
private val grupperingsId = "ARBEIDSPLASSEN"
private val tekst = "Du må oppdatere CV-en og jobbprofilen på arbeidsplassen.no"
private val sikkerhetsnivaa = 3

private val log = LoggerFactory.getLogger(BrukernotifikasjonProducer::class.java)

@Singleton
class BrukernotifikasjonProducer(
        private val brukernotifikasjonClient: BrukernotifikasjonClient,
        @Value("\${output.melding.link}") private val link: String
) : VarselPublisher {

    override fun publish(eventId: UUID, foedselsnummer: String) {
        log.info("Publiserer varsel med eventId $eventId")
        val nokkel = Nokkel(systembruker, eventId.toString())
        val oppgave = Oppgave(
                Instant.now().toEpochMilli(),
                foedselsnummer,
                grupperingsId,
                tekst,
                link,
                sikkerhetsnivaa
        )
        brukernotifikasjonClient.publish(nokkel, oppgave)
    }

    override fun done(eventId: UUID, foedselsnummer: String) {
        log.info("Publiserer donemelding for eventId $eventId")
        val nokkel = Nokkel(systembruker, eventId.toString())
        val done = Done(
                Instant.now().toEpochMilli(),
                foedselsnummer,
                grupperingsId
        )
        brukernotifikasjonClient.done(nokkel, done)
    }

}