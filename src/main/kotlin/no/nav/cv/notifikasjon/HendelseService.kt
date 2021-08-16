package no.nav.cv.notifikasjon

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Period
import java.time.ZonedDateTime

private val cutoffPeriod = Period.ofDays(2)

interface HendelseService {

    fun harKommetUnderOppfolging(aktorId: String, hendelsesTidspunkt: ZonedDateTime)

    fun ikkeUnderOppfolging(aktorId: String, hendelsesTidspunkt: ZonedDateTime)

    fun varsleBruker(aktorId: String)

    fun endretCV(aktorId: String, hendelsesTidspunkt: ZonedDateTime)

    fun funnetFodselsnummer(aktorId: String, fnr: String)
}


@Service
class Hendelser (
    private val repository: StatusRepository,
    private val varselPublisher: VarselPublisher,
) : HendelseService {

    private val log = LoggerFactory.getLogger(Hendelser::class.java)

    override fun harKommetUnderOppfolging(aktorId: String, datoSisteOppfolging: ZonedDateTime) {
        val cutoffTime = ZonedDateTime.now().minus(cutoffPeriod)

        val nyesteStatus = repository.finnSiste(aktorId)

        if(datoSisteOppfolging.isBefore(cutoffTime)) {
            log.debug("Oppfølgingsperiode: ${datoSisteOppfolging} startert før cutoff-perioden ($cutoffPeriod). Ignorerer oppfølgingsstatus")

            val nesteStatus = when(nyesteStatus.status) {
                nyBrukerStatus -> nyesteStatus.forGammel(datoSisteOppfolging)
                ikkeUnderOppfølgingStatus -> nyesteStatus.nySession().forGammel(datoSisteOppfolging)
                cvOppdatertStatus -> nyesteStatus.nySession().forGammel(datoSisteOppfolging)
                forGammelStatus -> nyesteStatus.nySession().forGammel(datoSisteOppfolging)

                else -> null
            }

            if(nesteStatus != null)
                repository.lagreNyStatus(nesteStatus)

            return
        }

        val nesteStatus = when(nyesteStatus.status) {
            nyBrukerStatus -> nyesteStatus.skalVarlsesManglerFnr(datoSisteOppfolging)
            forGammelStatus -> nyesteStatus.nySession().skalVarlsesManglerFnr(datoSisteOppfolging)
            ikkeUnderOppfølgingStatus -> nyesteStatus.nySession().skalVarlsesManglerFnr(datoSisteOppfolging)

            cvOppdatertStatus -> {
                if (nyesteStatus.statusTidspunkt.isBefore(datoSisteOppfolging))
                    nyesteStatus.nySession().skalVarlsesManglerFnr(datoSisteOppfolging)
                else null
            }

            else -> null // ikke gjør noe hvis personen er i noen av de andre statusene
        }

        if(nesteStatus != null)
            repository.lagreNyStatus(nesteStatus)
    }
//
//    override fun harKommetUnderOppfolging(aktorId: String, hendelsesTidspunkt: ZonedDateTime) {
//
//        val cutoffTime = ZonedDateTime.now().minus(cutoffPeriod)
//        if(hendelsesTidspunkt.isBefore(cutoffTime)) {
//            log.debug("Oppfølgingsperiode: ${hendelsesTidspunkt} startert før cutoff-perioden ($cutoffPeriod). Ignorerer oppfølgingsstatus")
//            return
//        }
//
//        val status = repository.finnSiste(aktorId)
//        val nesteStatus = status.harKommetUnderOppfolging(hendelsesTidspunkt, abTestSelector)
//
//        repository.lagre(nesteStatus)
//    }

    override fun ikkeUnderOppfolging(aktorId: String, datoSisteOppfolging: ZonedDateTime) {
        val nyesteStatus = repository.finnSiste(aktorId)

        // Bør vi sende denne uansett, i tilfelle vi skulle få en race condition mellom to statuser?
        val nesteStatus = when(nyesteStatus.status) {
            nyBrukerStatus -> nyesteStatus.ikkeUnderOppfølging(datoSisteOppfolging)

            skalVarslesManglerFnrStatus -> nyesteStatus.ikkeUnderOppfølging(datoSisteOppfolging)
            skalVarslesStatus -> nyesteStatus.ikkeUnderOppfølging(datoSisteOppfolging)

            varsletStatus -> {
                varselPublisher.done(nyesteStatus.uuid, nyesteStatus.fnr)
                nyesteStatus.ikkeUnderOppfølging(datoSisteOppfolging)
            }

            else -> null
        }

        if(nesteStatus != null)
            repository.lagreNyStatus(nesteStatus)
    }

//    override fun ikkeUnderOppfolging(aktorId: String, hendelsesTidspunkt: ZonedDateTime){
//        val status = repository.finnSiste(aktorId)
//        val nesteStatus = status.blittFulgtOpp(hendelsesTidspunkt, varselPublisher)
//        repository.lagre(nesteStatus)
//    }


    override fun endretCV(aktorId: String, tidspunkt: ZonedDateTime) {
        val nyesteStatus = repository.finnSiste(aktorId)

        if(nyesteStatus.status == varsletStatus)
            varselPublisher.done(nyesteStatus.uuid, nyesteStatus.fnr)

        repository.lagreNyStatus(nyesteStatus.endretCV(tidspunkt))
    }

//    override fun harSettCv(aktorId: String, hendelsesTidspunkt: ZonedDateTime) {
//
//        val cutoffTime = ZonedDateTime.now().minus(cutoffPeriod)
//        if(hendelsesTidspunkt.isBefore(cutoffTime)) {
//            log.debug("CV endret (${hendelsesTidspunkt} før cutoff-perioden ($cutoffPeriod). Ignorerer endringer")
//            return
//        }
//
//        val status = repository.finnSiste(aktorId)
//        val nesteStatus = status.harSettCv(hendelsesTidspunkt, varselPublisher)
//        repository.lagre(nesteStatus)
//    }


    override fun funnetFodselsnummer(aktorId: String, fnr: String) {
        val nyesteStatus = repository.finnSiste(aktorId)

        if(nyesteStatus.status != skalVarslesManglerFnrStatus) throw IllegalStateException("Skal ikke kunne finne fødselsnummer når statusen er noe annet enn $skalVarslesManglerFnrStatus. Gjelder status ${nyesteStatus.uuid}")

        if(fnr != ukjentFnr) repository.lagreNyStatus(nyesteStatus.skalVarsles(fnr))
    }

    override fun varsleBruker(aktorId: String) {
        val nyesteStatus = repository.finnSiste(aktorId)

        require(nyesteStatus.status == skalVarslesStatus) { "Kan kun varsle når status er $skalVarslesStatus. Gjelder status ${nyesteStatus.uuid}" }
        require(nyesteStatus.fnr != ukjentFnr) { "Trenger fødselsnummer når det skal varsles. Gjelder status ${nyesteStatus.uuid}" }

        varselPublisher.publish(nyesteStatus.uuid, nyesteStatus.fnr)
        repository.lagreNyStatus(nyesteStatus.varslet(ZonedDateTime.now()))
    }

//    override fun varsleBruker(aktorId: String, hendelsesTidspunkt: ZonedDateTime){
//        val status = repository.finnSiste(aktorId)
//        val nesteStatus = status.varsleBruker(varselPublisher)
//        repository.lagre(nesteStatus)
//    }




}

