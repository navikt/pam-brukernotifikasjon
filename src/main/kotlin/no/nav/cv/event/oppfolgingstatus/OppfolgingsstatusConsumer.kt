package no.nav.cv.event.oppfolgingstatus

import no.nav.cv.notifikasjon.HendelseService
import javax.inject.Singleton

@Singleton
class OppfolgingsstatusConsumer(
        private val hendelseService: HendelseService
) {

    fun mottaNoeFraCvTopic() {
        val tmpFnr = "12341234123"
        if(true) {
            hendelseService.kommetUnderOppfolging(tmpFnr)
        } else {
            hendelseService.erFulgtOpp(tmpFnr)
        }
    }

}
