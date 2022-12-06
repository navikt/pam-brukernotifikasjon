package no.nav.cv.notifikasjon


interface VarselPublisher {

    fun publish(
            eventId: String,
            foedselsnummer: String
    )

    fun done(
            eventId: String,
            foedselsnummer: String
    )
}