package no.nav.cv.notifikasjon

import org.slf4j.LoggerFactory
import java.time.ZonedDateTime
import java.util.*

const val ukjentFnr = "ukjent"

const val nyBrukerStatus = "ukjent" // "nyBruker"

const val skalVarslesManglerFnrStatus = "skalVarslesManglerFnr"
const val deprecatedSkalVarslesStatus = "skalVarsles"
const val varsletStatus = "varslet"

const val forGammelStatus = "forGammel"
const val cvOppdatertStatus = "cvOppdatert"
const val ikkeUnderOppfølgingStatus = "ikkeUnderOppfølging"

const val deprecatedDoneStatus = "done"

//const val ukjentFnr = "ukjent"
//
//const val nyBrukerStatus = "ukjent"
//const val skalVarslesStatus = "skalVarsles"
//const val varsletStatus = "varslet"
//const val doneStatus = "done"
//const val abTestSkalIkkeVarsles = "abTestSkalIkkeVarsles"

private val startOfTime = ZonedDateTime.now().minusYears(40)


class Status(
        val uuid: UUID,
        val aktoerId: String,
        val fnr: String = ukjentFnr,
        val status: String,
        val statusTidspunkt: ZonedDateTime
) {

    companion object {
        private val log = LoggerFactory.getLogger(Status::class.java)

        fun nySession(aktorId: String, tidspunkt: ZonedDateTime = startOfTime) = Status(
                uuid = UUID.randomUUID(),
                aktoerId = aktorId,
                status = nyBrukerStatus,
                statusTidspunkt = tidspunkt)

        fun skalVarlsesManglerFnr(forrigeStatus: Status, statusTidspunkt: ZonedDateTime) = Status(
                uuid = forrigeStatus.uuid,
                aktoerId = forrigeStatus.aktoerId,
                fnr = ukjentFnr, // vi tvinger frem nytt oppslag - i tilfelle fnr kan ha endret seg
                status = skalVarslesManglerFnrStatus,
                statusTidspunkt = statusTidspunkt)

        fun varslet(forrigeStatus: Status, foedselsnummer: String, statusTidspunkt: ZonedDateTime) = Status(
                uuid = forrigeStatus.uuid,
                aktoerId = forrigeStatus.aktoerId,
                fnr = foedselsnummer,
                status = varsletStatus,
                statusTidspunkt = statusTidspunkt)

        fun forGammel(forrigeStatus: Status, uuid: UUID = forrigeStatus.uuid, tidspunkt: ZonedDateTime) = Status(
                uuid = uuid,
                aktoerId = forrigeStatus.aktoerId,
                fnr = forrigeStatus.fnr,
                status = forGammelStatus,
                statusTidspunkt = tidspunkt)

        fun ikkeUnderOppfolging(forrigeStatus: Status, tidspunkt: ZonedDateTime) = Status(
                uuid = forrigeStatus.uuid,
                aktoerId = forrigeStatus.aktoerId,
                fnr = forrigeStatus.fnr,
                status = ikkeUnderOppfølgingStatus,
                statusTidspunkt = tidspunkt)

        fun endretCV(forrigeStatus: Status, tidspunkt: ZonedDateTime) = Status(
                uuid = forrigeStatus.uuid,
                aktoerId = forrigeStatus.aktoerId,
                fnr = forrigeStatus.fnr,
                status = cvOppdatertStatus,
                statusTidspunkt = tidspunkt)
    }

    fun nySession(): Status {
        return nySession(
            aktorId = this.aktoerId,
            tidspunkt = this.statusTidspunkt.plusNanos(1000)
        )
    }

    fun skalVarlsesManglerFnr(datoSisteOppfolging: ZonedDateTime): Status {
        return skalVarlsesManglerFnr(
            this,
            datoSisteOppfolging
        )
    }

    fun varslet(foedselsnummer: String, tidspunkt: ZonedDateTime): Status {
        return varslet(this, foedselsnummer, tidspunkt)
    }

    fun forGammel(datoSisteOppfolging: ZonedDateTime): Status {
        return forGammel(
            forrigeStatus = this,
            tidspunkt = datoSisteOppfolging
        )
    }

    fun ikkeUnderOppfølging(hendelsesTidspunkt: ZonedDateTime): Status {
        return ikkeUnderOppfolging(this, hendelsesTidspunkt)
    }

    fun endretCV(hendelsesTidspunkt: ZonedDateTime): Status {
        return endretCV(this, hendelsesTidspunkt)
    }

    override fun toString(): String {
        return "Status(uuid=$uuid, aktorId='$aktoerId', fnr='XXX', status='$status', statusTidspunkt=$statusTidspunkt)"
    }

}