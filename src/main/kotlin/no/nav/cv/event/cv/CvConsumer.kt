package no.nav.cv.event.cv

import no.nav.cv.notifikasjon.HendelseService
import javax.inject.Singleton

@Singleton
class CvConsumer(
        private val hendelseService: HendelseService
) {

    fun mottaNoeFraCvTopic() {
        val tmpFnr = "12341234123"
        hendelseService.settCv(tmpFnr)
    }

}
