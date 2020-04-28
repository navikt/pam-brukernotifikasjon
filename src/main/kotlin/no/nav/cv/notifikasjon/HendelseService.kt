package no.nav.cv.notifikasjon

import javax.inject.Singleton

@Singleton
class HendelseService(
    private val repository: StatusRepository,
    private val varselPublisher: VarselPublisher
){




    fun kommetUnderOppfolging(fnr: String) = behandle(
            Hendelse.kommetUnderOppfolging(fnr, sisteStatus(fnr)))
    fun erFulgtOpp(fnr: String) = behandle(
            Hendelse.erFulgtOpp(fnr, sisteStatus(fnr)))
    fun settCv(fnr: String) = behandle(
            Hendelse.settCv(fnr, sisteStatus(fnr)))

    private fun sisteStatus(fnr: String) = repository.finnSiste(fnr)

    private fun behandle(hendelse: Hendelse) {

        val nyStatus = hendelse.produserVarsel().varsle(varselPublisher)

        repository.lagre(nyStatus)
    }
}

