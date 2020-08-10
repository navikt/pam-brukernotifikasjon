package no.nav.cv.notifikasjon

import org.slf4j.LoggerFactory
import java.time.ZonedDateTime
import java.util.*

const val ukjentFnr = "ukjent"

const val nyBrukerStatus = "ukjent"
const val skalVarslesStatus = "skalVarsles"
const val varsletStatus = "varslet"
const val doneStatus = "done"
const val abTestSkalIkkeVarsles = "abTestSkalIkkeVarsles"

private val startOfTime = ZonedDateTime.now().minusYears(40)

class Status(
        val uuid: UUID,
        val aktorId: String,
        val fnr: String = ukjentFnr,
        val status: String,
        val statusTidspunkt: ZonedDateTime
) {

    companion object {
        private val log = LoggerFactory.getLogger(Status::class.java)

        fun nyBruker(aktorId: String) = Status(
                uuid = UUID.randomUUID(),
                aktorId = aktorId,
                status = nyBrukerStatus,
                statusTidspunkt = startOfTime)

        fun nyttVarsel(forrigeStatus: Status) = Status(
                uuid = UUID.randomUUID(),
                aktorId = forrigeStatus.aktorId,
                status = nyBrukerStatus,
                statusTidspunkt = forrigeStatus.statusTidspunkt.plusNanos(1000))

        fun skalVarsles(forrigeStatus: Status, statusTidspunkt: ZonedDateTime) = Status(
                uuid = forrigeStatus.uuid,
                aktorId = forrigeStatus.aktorId,
                fnr = forrigeStatus.fnr,
                status = skalVarslesStatus,
                statusTidspunkt = statusTidspunkt)

        fun varslet(forrigeStatus: Status, fnr: String, statusTidspunkt: ZonedDateTime) = Status(
                uuid = forrigeStatus.uuid,
                aktorId = forrigeStatus.aktorId,
                fnr = fnr,
                status = varsletStatus,
                statusTidspunkt = statusTidspunkt)

        fun done(forrigeStatus: Status, statusTidspunkt: ZonedDateTime) = Status(
                uuid = forrigeStatus.uuid,
                aktorId = forrigeStatus.aktorId,
                fnr = forrigeStatus.fnr,
                status = doneStatus,
                statusTidspunkt = statusTidspunkt)

        fun funnetFodselsnummer(forrigeStatus: Status, fodselsnummer: String) = Status(
                uuid = forrigeStatus.uuid,
                aktorId = forrigeStatus.aktorId,
                fnr = fodselsnummer,
                status = forrigeStatus.status,
                statusTidspunkt = ZonedDateTime.now())

        fun abTestSkalIkkeVarsles(forrigeStatus: Status) = Status(
                uuid = forrigeStatus.uuid,
                aktorId = forrigeStatus.aktorId,
                fnr = forrigeStatus.fnr,
                status = abTestSkalIkkeVarsles,
                statusTidspunkt = ZonedDateTime.now())
    }

    fun isAfter(tidspunkt: ZonedDateTime): Boolean = statusTidspunkt.isAfter(tidspunkt)

    fun erVarslet(): Boolean = status == varsletStatus

    fun harKommetUnderOppfolging(hendelsesTidspunkt: ZonedDateTime, abTestSelector: ABTestSelector): Status {

        val nyStatus = when(status) {
            doneStatus -> skalVarsles(nyttVarsel(this), hendelsesTidspunkt)
            skalVarslesStatus -> skalVarsles(nyttVarsel(this), hendelsesTidspunkt)

            nyBrukerStatus -> if(abTestSelector.skalVarsles()) skalVarsles(this, hendelsesTidspunkt)
                                else abTestSkalIkkeVarsles(this)
            else -> this
        }

        return nyStatus
    }

    fun funnetFodselsnummer(fodselsnummer: String): Status {
        return funnetFodselsnummer(this, fodselsnummer = fodselsnummer)
    }

    fun varsleBruker(
            varselPublisher: VarselPublisher
    ): Status {
        if(status != skalVarslesStatus) {
            log.debug("UUID ($uuid) er alt varslet og har status $status")
            return this
        }

        varselPublisher.publish(uuid, fnr)
        return varslet(this, fnr, ZonedDateTime.now())
    }

    fun blittFulgtOpp(
            hendelsesTidspunkt: ZonedDateTime,
            varselPublisher: VarselPublisher
    ): Status {
        if(status == varsletStatus) {
            varselPublisher.done(uuid, fnr)
        }
        return done(this, hendelsesTidspunkt)
    }
    fun harSettCv(
            hendelsesTidspunkt: ZonedDateTime,
            varselPublisher: VarselPublisher
    ): Status {
        if(status == varsletStatus) {
            varselPublisher.done(uuid, fnr)
        }
        return done(this, hendelsesTidspunkt)

    }

    override fun toString(): String {
        return "Status(uuid=$uuid, aktorId='$aktorId', fnr='$fnr', status='$status', statusTidspunkt=$statusTidspunkt)"
    }


}