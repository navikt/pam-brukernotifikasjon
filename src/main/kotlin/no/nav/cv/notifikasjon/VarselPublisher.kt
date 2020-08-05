package no.nav.cv.notifikasjon

import java.util.*

interface VarselPublisher {

    fun publish(
            eventId: UUID,
            foedselsnummer: String
    )

    fun done(
            eventId: UUID,
            foedselsnummer: String
    )

    fun done(
            eventId: UUID,
            foedselsnummer: String,
            systembruker: String
    )

}