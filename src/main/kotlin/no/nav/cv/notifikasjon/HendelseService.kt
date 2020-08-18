package no.nav.cv.notifikasjon

import no.nav.cv.event.oppfolgingstatus.OppfolgingstatusService
import java.time.Period
import java.time.ZonedDateTime
import javax.inject.Singleton

private val cutoffPeriod = Period.ofDays(2)

interface HendelseService {

    fun harKommetUnderOppfolging(aktorId: String, hendelsesTidspunkt: ZonedDateTime)

    fun funnetFodselsnummer(aktorId: String, fnr: String)

    fun varsleBruker(aktorId: String, hendelsesTidspunkt: ZonedDateTime)

    fun blittFulgtOpp(aktorId: String, hendelsesTidspunkt: ZonedDateTime)

    fun harSettCv(aktorId: String, hendelsesTidspunkt: ZonedDateTime)

}


@Singleton
class Hendelser (
    private val repository: StatusRepository,
    private val varselPublisher: VarselPublisher
) {
    val cutoffTime = ZonedDateTime.now().minus(cutoffPeriod)

    fun harKommetUnderOppfolging(aktorId: String, datoSisteOppfolging: ZonedDateTime) {
        val statuser = repository.finnStatuser(aktorId)

        val nyesteStatus = statuser.nyesteStatus()

        if(datoSisteOppfolging.isBefore(cutoffTime)) {
            OppfolgingstatusService.log.debug("Oppfølgingsperiode: ${datoSisteOppfolging} startert før cutoff-perioden ($cutoffPeriod). Ignorerer oppfølgingsstatus")
            if(nyesteStatus.status == nyBrukerStatus)
                nyesteStatus.forGammel(datoSisteOppfolging)

            return
        }

        val nesteStatus = when(nyesteStatus.status) {
            nyBrukerStatus -> nyesteStatus.skalVarlsesManglerFnr(datoSisteOppfolging)
            forGammelStatus -> nyesteStatus.nySession().skalVarlsesManglerFnr(datoSisteOppfolging)
            ikkeUnderOppfølgingStatus -> nyesteStatus.nySession().skalVarlsesManglerFnr(datoSisteOppfolging)

            cvOppdatertStatus -> {
                if (statuser.cvOppdatertTidspunkt().isBefore(datoSisteOppfolging))
                    nyesteStatus.nySession().skalVarlsesManglerFnr(datoSisteOppfolging)
                else null
            }

            else -> null // ikke gjør noe hvis personen er i noen av de andre statusene
        }

        if(nesteStatus != null)
            repository.lagre(nesteStatus)
    }

    fun funnetFodselsnummer(aktorId: String, fnr: String) {
        val statuser = repository.finnStatuser(aktorId)

        if(statuser.nyesteStatus().status != skalVarslesManglerFnrStatus) throw IllegalStateException("Skal ikke kunne finne fødselsnummer når statusen er noe annet enn $skalVarslesManglerFnrStatus. Gjelder status $statuser.uuid")

        if(fnr != ukjentFnr) repository.lagre(statuser.nyesteStatus().funnetFodselsnummer(fnr))
    }

    fun ikkeUnderOppfolging(aktorId: String, datoSisteOppfolging: ZonedDateTime) {
        val statuser = repository.finnStatuser(aktorId)

        val nyesteStatus = statuser.nyesteStatus()

        // Bør vi sende denne uansett, i tilfelle vi skulle få en race condition mellom to statuser?
        if(nyesteStatus.status == varsletStatus)
            varselPublisher.done(nyesteStatus.uuid, nyesteStatus.fnr)

        repository.lagre(nyesteStatus.ikkeUnderOppfølging(datoSisteOppfolging))
    }

    fun settCv(aktorId: String, tidspunkt: ZonedDateTime) {
        val nyesteStatus = repository.finnStatuser(aktorId).nyesteStatus()

        if(nyesteStatus.status == varsletStatus)
            varselPublisher.done(nyesteStatus.uuid, nyesteStatus.fnr)

        repository.lagre(nyesteStatus.harSettCv(tidspunkt))

    }

    fun varsleBruker(aktorId: String) {
        val statuser = repository.finnStatuser(aktorId)

        val nyesteStatus = statuser.nyesteStatus()

        require(nyesteStatus.status == skalVarslesStatus) { "Kan kun varsle når status er $skalVarslesStatus. Gjelder status $statuser.uuid" }
        require(nyesteStatus.fnr != ukjentFnr) { "Trenger fødselsnummer når det skal varsles. Gjelder status $statuser.uuid" }

        varselPublisher.publish(nyesteStatus.uuid, nyesteStatus.fnr)
        repository.lagre(Status.varslet(nyesteStatus, ZonedDateTime.now()))
    }
}


class HendelserLegacy (
    private val repository: StatusRepository,
    private val varselPublisher: VarselPublisher,
    private val abTestSelector: ABTestSelector
) /*: HendelseService */{

     fun kommetUnderOppfolging(aktorId: String, hendelsesTidspunkt: ZonedDateTime) {

        val cutoffTime = ZonedDateTime.now().minus(cutoffPeriod)
        if(hendelsesTidspunkt.isBefore(cutoffTime)) {
            OppfolgingstatusService.log.debug("Oppfølgingsperiode: ${hendelsesTidspunkt} startert før cutoff-perioden ($cutoffPeriod). Ignorerer oppfølgingsstatus")
            return
        }

        val status = repository.finnSiste(aktorId)
        val nesteStatus = status.harKommetUnderOppfolging(hendelsesTidspunkt, abTestSelector)

        repository.lagre(nesteStatus)
    }

     fun varsleBruker(aktorId: String, hendelsesTidspunkt: ZonedDateTime){
        val status = repository.finnSiste(aktorId)
        val nesteStatus = status.varsleBruker(varselPublisher)
        repository.lagre(nesteStatus)
    }

     fun blittFulgtOpp(aktorId: String, hendelsesTidspunkt: ZonedDateTime){
        val status = repository.finnSiste(aktorId)
        val nesteStatus = status.ikkeUnderOppfølging(hendelsesTidspunkt, varselPublisher)
        repository.lagre(nesteStatus)
    }

     fun harSettCv(aktorId: String, hendelsesTidspunkt: ZonedDateTime) {

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

