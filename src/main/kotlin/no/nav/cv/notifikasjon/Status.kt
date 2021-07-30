package no.nav.cv.notifikasjon

import org.slf4j.LoggerFactory
import java.time.ZonedDateTime
import java.util.*

const val ukjentFnr = "ukjent"


const val nyBrukerStatus = "ukjent" // "nyBruker"
const val skalVarslesManglerFnrStatus = "skalVarslesManglerFnr"
const val skalVarslesStatus = "skalVarsles"
const val varsletStatus = "varslet"

const val forGammelStatus = "forGammel"
const val cvOppdatertStatus = "cvOppdatert"
const val ikkeUnderOppfølgingStatus = "ikkeUnderOppfølging"

const val doneStatus = "done"

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
            statusTidspunkt = tidspunkt
        )

        fun skalVarlsesManglerFnr(forrigeStatus: Status, statusTidspunkt: ZonedDateTime) = Status(
            uuid = forrigeStatus.uuid,
            aktoerId = forrigeStatus.aktoerId,
            fnr = ukjentFnr, // vi tvinger frem nytt oppslag - i tilfelle fnr kan ha endret seg
            status = skalVarslesManglerFnrStatus,
            statusTidspunkt = statusTidspunkt
        )

        fun skalVarsles(forrigeStatus: Status, fodselsnummer: String) = Status(
            uuid = forrigeStatus.uuid,
            aktoerId = forrigeStatus.aktoerId,
            fnr = fodselsnummer,
            status = skalVarslesStatus,
            statusTidspunkt = ZonedDateTime.now()
        )

        fun varslet(forrigeStatus: Status, statusTidspunkt: ZonedDateTime) = Status(
            uuid = forrigeStatus.uuid,
            aktoerId = forrigeStatus.aktoerId,
            fnr = forrigeStatus.fnr,
            status = varsletStatus,
            statusTidspunkt = statusTidspunkt
        )

        fun forGammel(forrigeStatus: Status, uuid: UUID = forrigeStatus.uuid, tidspunkt: ZonedDateTime) = Status(
            uuid = uuid,
            aktoerId = forrigeStatus.aktoerId,
            fnr = forrigeStatus.fnr,
            status = forGammelStatus,
            statusTidspunkt = tidspunkt
        )

        fun ikkeUnderOppfolging(forrigeStatus: Status, tidspunkt: ZonedDateTime) = Status(
            uuid = forrigeStatus.uuid,
            aktoerId = forrigeStatus.aktoerId,
            fnr = forrigeStatus.fnr,
            status = ikkeUnderOppfølgingStatus,
            statusTidspunkt = tidspunkt
        )

        fun cvOppdatert(forrigeStatus: Status, tidspunkt: ZonedDateTime) = Status(
            uuid = forrigeStatus.uuid,
            aktoerId = forrigeStatus.aktoerId,
            fnr = forrigeStatus.fnr,
            status = cvOppdatertStatus,
            statusTidspunkt = tidspunkt
        )
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

    fun skalVarsles(fodselsnummer: String): Status {
        return skalVarsles(this, fodselsnummer = fodselsnummer)
    }

    fun varslet(tidspunkt: ZonedDateTime): Status {
        return varslet(this, tidspunkt)
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

    fun harSettCv(hendelsesTidspunkt: ZonedDateTime): Status {
        return cvOppdatert(this, hendelsesTidspunkt)
    }

    override fun toString(): String {
        return "Status(uuid=$uuid, aktorId='$aktoerId', fnr='XXX', status='$status', statusTidspunkt=$statusTidspunkt)"
    }

}

/**
 * Representerer en gruppe med statuser som hører sammen
 */
class Statuser(
    private val statuser: List<Status>
) {
    init {
        require(statuser.all { it.uuid == statuser[0].uuid })
    }

    fun nyesteStatus() = statuser.maxByOrNull { it.statusTidspunkt }
        ?: throw Exception("No status in the list?")

    fun cvOppdatertTidspunkt(): ZonedDateTime {
        return statuser
            .filter { it.status == cvOppdatertStatus }
            .map { it.statusTidspunkt }
            .maxByOrNull { it }
            ?: startOfTime
    }

    val uuid = statuser[0].uuid
}

fun List<Status>.statuser() = Statuser(this)
