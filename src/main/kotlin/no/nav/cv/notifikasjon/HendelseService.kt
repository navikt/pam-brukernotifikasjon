package no.nav.cv.notifikasjon

import no.nav.cv.output.OutboxService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Period
import java.time.ZonedDateTime

private val cutoffPeriod = Period.ofDays(2)

interface HendelseService {

    fun harKommetUnderOppfolging(aktorId: String, hendelsesTidspunkt: ZonedDateTime)

    fun ikkeUnderOppfolging(aktorId: String, hendelsesTidspunkt: ZonedDateTime)

    fun endretCV(aktorId: String, hendelsesTidspunkt: ZonedDateTime)

    fun funnetFodselsnummer(aktorId: String, fnr: String)
}


@Service
class Hendelser (
    private val repository: StatusRepository,
    private val outboxService: OutboxService,
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

            deprecatedDoneStatus -> {
                if (nyesteStatus.statusTidspunkt.isBefore(datoSisteOppfolging))
                    nyesteStatus.nySession().skalVarlsesManglerFnr(datoSisteOppfolging)
                else null
            }

            else -> null // ikke gjør noe hvis personen er i noen av de andre statusene
        }

        if(nesteStatus != null)
            repository.lagreNyStatus(nesteStatus)
    }

    override fun ikkeUnderOppfolging(aktorId: String, datoSisteOppfolging: ZonedDateTime) {
        val nyesteStatus = repository.finnSiste(aktorId)

        // Bør vi sende denne uansett, i tilfelle vi skulle få en race condition mellom to statuser?
        val nesteStatus = when(nyesteStatus.status) {
            nyBrukerStatus -> nyesteStatus.ikkeUnderOppfølging(datoSisteOppfolging)

            skalVarslesManglerFnrStatus -> nyesteStatus.ikkeUnderOppfølging(datoSisteOppfolging)
            deprecatedSkalVarslesStatus -> nyesteStatus.ikkeUnderOppfølging(datoSisteOppfolging)

            varsletStatus -> {
                outboxService.schdeuleDone(nyesteStatus.uuid, nyesteStatus.aktoerId, nyesteStatus.fnr)
                nyesteStatus.ikkeUnderOppfølging(datoSisteOppfolging)
            }

            else -> null
        }

        if(nesteStatus != null)
            repository.lagreNyStatus(nesteStatus)
    }

    override fun endretCV(aktorId: String, tidspunkt: ZonedDateTime) {
        val nyesteStatus = repository.finnSiste(aktorId)

        if(nyesteStatus.status == varsletStatus)
            outboxService.schdeuleDone(nyesteStatus.uuid, nyesteStatus.aktoerId, nyesteStatus.fnr)

        if(nyesteStatus.status != cvOppdatertStatus)
            repository.lagreNyStatus(nyesteStatus.endretCV(tidspunkt))
        else
            repository.oppdaterTidspunkt(aktorId, tidspunkt)
    }

    override fun funnetFodselsnummer(aktorId: String, fnr: String) {
        val currentStatus = repository.finnSiste(aktorId)

        if(currentStatus.status != skalVarslesManglerFnrStatus) throw IllegalStateException("Skal ikke kunne finne fødselsnummer når statusen er noe annet enn $skalVarslesManglerFnrStatus. Gjelder status ${currentStatus.uuid}")

        // Try again if fnr is unknown
        if(fnr == ukjentFnr)
            return

        val newStatus = currentStatus.varslet(fnr, ZonedDateTime.now())

        outboxService.schdeuleVarsel(newStatus.uuid, newStatus.aktoerId, newStatus.fnr)
        repository.lagreNyStatus(newStatus)
    }
}

