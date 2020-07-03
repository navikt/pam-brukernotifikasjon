package no.nav.cv.notifikasjon

import no.nav.cv.event.oppfolgingstatus.OppfolgingstatusService
import no.nav.cv.person.PersonIdentRepository
import java.time.Period
import java.time.ZonedDateTime
import javax.inject.Named
import javax.inject.Qualifier
import javax.inject.Singleton

private val cutoffPeriod = Period.ofMonths(6)

interface HendelseService {

    fun kommetUnderOppfolging(aktorId: String, hendelsesTidspunkt: ZonedDateTime)

    fun varsleBruker(aktorId: String, hendelsesTidspunkt: ZonedDateTime)

    fun blittFulgtOpp(aktorId: String, hendelsesTidspunkt: ZonedDateTime)

    fun harSettCv(aktorId: String, hendelsesTidspunkt: ZonedDateTime)

}


@Singleton
class Hendelser (
    private val repository: StatusRepository,
    private val varselPublisher: VarselPublisher,
    private val personIdentRepository: PersonIdentRepository,
    private val abTestSelector: ABTestSelector
) : HendelseService {

    override fun kommetUnderOppfolging(aktorId: String, hendelsesTidspunkt: ZonedDateTime) {

        val cutoffTime = ZonedDateTime.now().minus(cutoffPeriod)
        if(hendelsesTidspunkt.isBefore(cutoffTime)) {
            // TODO: Fjern aktorId
            OppfolgingstatusService.log.debug("Oppfølgingsperiode for $aktorId : ${hendelsesTidspunkt} startert før cutoff-perioden ($cutoffPeriod). Ignorerer oppfølgingsstatus")
            return
        }

        val status = repository.finnSiste(aktorId)
        val nesteStatus = status.harKommetUnderOppfolging(hendelsesTidspunkt, abTestSelector)
        // TODO: Fjern før prod
        OppfolgingstatusService.log.debug("kommetUnderOppfolging: $aktorId går fra $status til $nesteStatus")

        repository.lagre(nesteStatus)
    }

    override fun varsleBruker(aktorId: String, hendelsesTidspunkt: ZonedDateTime){
        val status = repository.finnSiste(aktorId)
        val nesteStatus = status.varsleBruker(varselPublisher)
        repository.lagre(nesteStatus)
    }

    override fun blittFulgtOpp(aktorId: String, hendelsesTidspunkt: ZonedDateTime){
        val status = repository.finnSiste(aktorId)
        val nesteStatus = status.blittFulgtOpp(hendelsesTidspunkt, varselPublisher)
        // TODO: Fjern før prod
        OppfolgingstatusService.log.debug("blittFulgtOpp: $aktorId går fra $status til $nesteStatus")
        repository.lagre(nesteStatus)
    }

    override fun harSettCv(aktorId: String, hendelsesTidspunkt: ZonedDateTime) {

        val cutoffTime = ZonedDateTime.now().minus(cutoffPeriod)
        if(hendelsesTidspunkt.isBefore(cutoffTime)) {
            OppfolgingstatusService.log.debug("CV endret (${hendelsesTidspunkt} før cutoff-perioden ($cutoffPeriod). Ignorerer endringer")
            return
        }

        val status = repository.finnSiste(aktorId)
        val nesteStatus = status.harSettCv(hendelsesTidspunkt, varselPublisher)
        repository.lagre(nesteStatus)
    }

}

