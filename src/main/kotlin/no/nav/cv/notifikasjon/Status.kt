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
const val abTestSkalIkkeVarsles = "abTestSkalIkkeVarsles"

private val startOfTime = ZonedDateTime.now().minusYears(40)

/**
 * Representerer en gruppe med statuser som hører sammen
 */
class Statuser(
        private val statuser : List<Status>
) {
    init {
        require(statuser.isNotEmpty())
        require(statuser.all { it.uuid == statuser[0].uuid })
    }

    fun nyesteStatus() = statuser.sortedByDescending { it.statusTidspunkt }.first()

    fun cvOppdatertTidspunkt(): ZonedDateTime {
        return statuser
                .filter { it.status == cvOppdatertStatus}
                .map { it.statusTidspunkt }
                .maxBy { it }
                ?: startOfTime
    }

    val uuid = statuser[0].uuid
}

fun List<Status>.statuser() = Statuser(this)

class Status(
        val uuid: UUID,
        val aktorId: String,
        val fnr: String = ukjentFnr,
        val status: String,
        val statusTidspunkt: ZonedDateTime
) {

    companion object {
        private val log = LoggerFactory.getLogger(Status::class.java)

        fun nySession(aktorId: String, tidspunkt: ZonedDateTime = startOfTime) = Status(
                uuid = UUID.randomUUID(),
                aktorId = aktorId,
                status = nyBrukerStatus,
                statusTidspunkt = tidspunkt)

         fun skalVarlsesManglerFnr(forrigeStatus: Status, statusTidspunkt: ZonedDateTime) = Status(
                uuid = forrigeStatus.uuid,
                aktorId = forrigeStatus.aktorId,
                fnr = ukjentFnr, // vi tvinger frem nytt oppslag - i tilfelle fnr kan ha endret seg
                status = skalVarslesManglerFnrStatus,
                statusTidspunkt = statusTidspunkt)

        fun varslet(forrigeStatus: Status, statusTidspunkt: ZonedDateTime) = Status(
                uuid = forrigeStatus.uuid,
                aktorId = forrigeStatus.aktorId,
                fnr = forrigeStatus.fnr,
                status = varsletStatus,
                statusTidspunkt = statusTidspunkt)

        fun skalVarsles(forrigeStatus: Status, fodselsnummer: String) = Status(
                uuid = forrigeStatus.uuid,
                aktorId = forrigeStatus.aktorId,
                fnr = fodselsnummer,
                status = skalVarslesStatus,
                statusTidspunkt = ZonedDateTime.now())

        fun forGammel(forrigeStatus: Status, uuid: UUID = forrigeStatus.uuid, tidspunkt: ZonedDateTime) = Status(
                uuid = uuid,
                aktorId = forrigeStatus.aktorId,
                fnr = forrigeStatus.fnr,
                status = forGammelStatus,
                statusTidspunkt = tidspunkt
        )

        fun ikkeUnderOppfolging(forrigeStatus: Status, tidspunkt: ZonedDateTime) = Status(
                uuid = forrigeStatus.uuid,
                aktorId = forrigeStatus.aktorId,
                fnr = forrigeStatus.fnr,
                status = ikkeUnderOppfølgingStatus,
                statusTidspunkt = tidspunkt
        )

        fun cvOppdatert(forrigeStatus: Status, tidspunkt: ZonedDateTime) = Status(
                uuid = forrigeStatus.uuid,
                aktorId = forrigeStatus.aktorId,
                fnr = forrigeStatus.fnr,
                status = cvOppdatertStatus,
                statusTidspunkt = tidspunkt
        )
    }

    fun skalVarsles(fodselsnummer: String): Status {
        return skalVarsles(this, fodselsnummer = fodselsnummer)
    }

    fun ikkeUnderOppfølging(hendelsesTidspunkt: ZonedDateTime): Status {
        return ikkeUnderOppfolging(this, hendelsesTidspunkt)
    }

    fun harSettCv(hendelsesTidspunkt: ZonedDateTime): Status {
        return cvOppdatert(this, hendelsesTidspunkt)
    }

    fun forGammel(datoSisteOppfolging: ZonedDateTime) : Status {
        return forGammel(
                forrigeStatus = this,
                tidspunkt = datoSisteOppfolging)
    }

    fun skalVarlsesManglerFnr(datoSisteOppfolging: ZonedDateTime) : Status {
        return skalVarlsesManglerFnr(
                this,
                datoSisteOppfolging)
    }

    fun nySession() : Status {
        return nySession(
                aktorId = this.aktorId,
                tidspunkt = this.statusTidspunkt.plusNanos(1000)
        )
    }

    override fun toString(): String {
        return "Status(uuid=$uuid, aktorId='$aktorId', fnr='$fnr', status='$status', statusTidspunkt=$statusTidspunkt)"
    }


}