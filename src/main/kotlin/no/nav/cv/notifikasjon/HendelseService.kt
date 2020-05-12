package no.nav.cv.notifikasjon

import java.time.ZonedDateTime
import javax.inject.Singleton

interface HendelseService {

    fun kommetUnderOppfolging(fnr: String, timestamp: ZonedDateTime)

    fun erFulgtOpp(fnr: String, timestamp: ZonedDateTime)

    fun settCv(fnr: String, timestamp: ZonedDateTime)

}

@Singleton
class Hendelser (
    private val repository: StatusRepository,
    private val varselPublisher: VarselPublisher
) : HendelseService {

    override fun kommetUnderOppfolging(fnr: String, timestamp: ZonedDateTime) = behandle(
            Hendelse.kommetUnderOppfolging(fnr, sisteStatus(fnr), timestamp))
    override fun erFulgtOpp(fnr: String, timestamp: ZonedDateTime) = behandle(
            Hendelse.erFulgtOpp(fnr, sisteStatus(fnr), timestamp))
    override fun settCv(fnr: String, timestamp: ZonedDateTime) = behandle(
            Hendelse.settCv(fnr, sisteStatus(fnr), timestamp))

    private fun sisteStatus(fnr: String) = repository.finnSiste(fnr)

    private fun behandle(hendelse: Hendelse) {

        val nyStatus = hendelse.produserVarsel().varsle(varselPublisher)

        repository.lagre(nyStatus)
    }
}

