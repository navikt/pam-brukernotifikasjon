package no.nav.cv.person

import io.micronaut.scheduling.annotation.Scheduled
import no.nav.cv.notifikasjon.HendelseService
import no.nav.cv.notifikasjon.Status
import no.nav.cv.notifikasjon.StatusRepository
import java.time.ZonedDateTime
import javax.inject.Singleton


@Singleton
class FodselsnummerJobb(
        private val statusRepository: StatusRepository,
        private val personIdentRepository: PersonIdentRepository
) {

    @Scheduled(fixedDelay = "15s")
    fun fyllInnFnr() {
        statusRepository.manglerFodselsnummer()
                .forEach {
                    val fnr = personIdentRepository.finnIdenter(it.aktorId)
                            .gjeldende(PersonIdent.Type.FOLKEREGISTER)

                    fnr ?: return@forEach

                    val nyStatus = Status(
                            uuid = it.uuid,
                            aktorId = it.aktorId,
                            fnr = fnr,
                            status = it.status,
                            statusTidspunkt = ZonedDateTime.now(),
                            fortsettTidspunkt = it.fortsettTidspunkt)

                    statusRepository.lagre(nyStatus)
                }


    }
}