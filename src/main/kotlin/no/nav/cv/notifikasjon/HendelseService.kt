package no.nav.cv.notifikasjon

import no.nav.cv.output.OutboxService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Period
import java.time.ZonedDateTime

private val cutoffPeriod = Period.ofDays(2)

interface HendelseService {
    fun harKommetUnderOppfolging(aktorId: String, startdatoSisteOppfolging: ZonedDateTime)
    fun ikkeUnderOppfolging(aktorId: String, sluttdatoSisteOppfolging: ZonedDateTime)
    fun endretCV(aktorId: String, hendelsesTidspunkt: ZonedDateTime)
    fun funnetFodselsnummer(aktorId: String, fnr: String)
}

@Service
class Hendelser(
    private val repository: StatusRepository,
    private val outboxService: OutboxService,
) : HendelseService {
    private val log = LoggerFactory.getLogger(Hendelser::class.java)

    override fun harKommetUnderOppfolging(aktorId: String, startdatoSisteOppfolging: ZonedDateTime) {
        log.info("Mottok oppfolgingstatus for $aktorId - Oppfølging startet")
        val cutoffTime = ZonedDateTime.now().minus(cutoffPeriod)
        val nyesteStatus = repository.finnSiste(aktorId)

        if (startdatoSisteOppfolging.isBefore(cutoffTime)) {
            log.info("Oppfølgingsperiode: ${startdatoSisteOppfolging} startert før cutoff-perioden ($cutoffPeriod). Ignorerer oppfølgingsstatus")

            val nesteStatus = when (nyesteStatus.status) {
                nyBrukerStatus -> nyesteStatus.forGammel(startdatoSisteOppfolging)
                ikkeUnderOppfølgingStatus,
                cvOppdatertStatus,
                forGammelStatus -> nyesteStatus.nySession().forGammel(startdatoSisteOppfolging)

                else -> null
            }

            nesteStatus?.let { repository.lagreNyStatus(it) }
            return
        }

        val nesteStatus = when (nyesteStatus.status) {
            nyBrukerStatus -> nyesteStatus.skalVarlsesManglerFnr(startdatoSisteOppfolging)
            forGammelStatus,
            ikkeUnderOppfølgingStatus -> nyesteStatus.nySession().skalVarlsesManglerFnr(startdatoSisteOppfolging)

            cvOppdatertStatus,
            deprecatedDoneStatus -> {
                if (nyesteStatus.statusTidspunkt.isBefore(startdatoSisteOppfolging))
                    nyesteStatus.nySession().skalVarlsesManglerFnr(startdatoSisteOppfolging)
                else null
            }

            else -> null // ikke gjør noe hvis personen er i noen av de andre statusene
        }

        nesteStatus?.let { repository.lagreNyStatus(it) }
    }

    override fun ikkeUnderOppfolging(aktorId: String, sluttdatoSisteOppfolging: ZonedDateTime) {
        log.info("Mottok oppfølgingstatus for $aktorId - Oppfølging avsluttet")

        val nyesteStatus = repository.finnSiste(aktorId)

        // Bør vi sende denne uansett, i tilfelle vi skulle få en race condition mellom to statuser?
        val nesteStatus = when (nyesteStatus.status) {
            nyBrukerStatus,
            skalVarslesManglerFnrStatus,
            deprecatedSkalVarslesStatus -> nyesteStatus.ikkeUnderOppfølging(sluttdatoSisteOppfolging)

            varsletStatus -> {
                outboxService.schdeuleDone(nyesteStatus.uuid, nyesteStatus.aktoerId, nyesteStatus.fnr)
                nyesteStatus.ikkeUnderOppfølging(sluttdatoSisteOppfolging)
            }

            else -> null
        }

        nesteStatus?.let { repository.lagreNyStatus(it) }
    }

    override fun endretCV(aktorId: String, hendelsesTidspunkt: ZonedDateTime) {
        val nyesteStatus = repository.finnSiste(aktorId)

        if (nyesteStatus.status == varsletStatus)
            outboxService.schdeuleDone(nyesteStatus.uuid, nyesteStatus.aktoerId, nyesteStatus.fnr)

        if (nyesteStatus.status != cvOppdatertStatus)
            repository.lagreNyStatus(nyesteStatus.endretCV(hendelsesTidspunkt))
        else
            repository.oppdaterTidspunkt(aktorId, hendelsesTidspunkt)
    }

    override fun funnetFodselsnummer(aktorId: String, fnr: String) {
        val currentStatus = repository.finnSiste(aktorId)

        if (currentStatus.status != skalVarslesManglerFnrStatus) throw IllegalStateException("Skal ikke kunne finne fødselsnummer når statusen er noe annet enn $skalVarslesManglerFnrStatus. Gjelder status ${currentStatus.uuid}")

        // Try again if fnr is unknown
        if (fnr == ukjentFnr)
            return

        val newStatus = currentStatus.varslet(fnr, ZonedDateTime.now())

        outboxService.schdeuleVarsel(newStatus.uuid, newStatus.aktoerId, newStatus.fnr)
        repository.lagreNyStatus(newStatus)
    }
}

