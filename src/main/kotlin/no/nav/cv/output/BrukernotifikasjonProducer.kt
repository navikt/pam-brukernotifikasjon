package no.nav.cv.output

import no.nav.brukernotifikasjon.schemas.Done
import no.nav.brukernotifikasjon.schemas.Nokkel
import no.nav.brukernotifikasjon.schemas.Oppgave
import no.nav.cv.notifikasjon.VarselPublisher
import java.time.Instant
import java.util.*
import javax.inject.Singleton

private val systembruker = "enSystemBruker"
private val grupperingsId = "ARBEIDSPLASSEN"
private val tekst = "På tide å gi CVen litt kjærlighet"
private val link = "https://arbeidsplassen.nav.no/cv/registrering"
private val sikkerhetsnivaa = 4

@Singleton
class BrukernotifikasjonProducer(
        private val brukernotifikasjonClient: BrukernotifikasjonClient
) : VarselPublisher {

    override fun publish(eventId: UUID, foedselsnummer: String) {

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
        val nokkel = Nokkel(systembruker, eventId.toString())
        val done = Done(
                Instant.now().toEpochMilli(),
                foedselsnummer,
                grupperingsId
        )
        brukernotifikasjonClient.done(nokkel, done)
    }

}