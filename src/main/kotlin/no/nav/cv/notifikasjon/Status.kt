package no.nav.cv.notifikasjon

import java.time.ZonedDateTime
import java.util.*

const val ukjentFnr = "ukjent"
const val nyBrukerStatus = "ukjent"
const val skalVarslesStatus = "skalVarsles"
const val varsletStatus = "varslet"
const val doneStatus = "done"
const val ignorertHendelseStatus = "ignorert"
private val startOfTime = ZonedDateTime.now().minusYears(40)

class Status(
        val uuid: UUID,
        val aktorId: String,
        val fnr: String = ukjentFnr,
        val status: String,
        val statusTidspunkt: ZonedDateTime
) {

    companion object {
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

        fun ignorert(aktorId: String) = Status(
                uuid = UUID.randomUUID(),
                aktorId = aktorId,
                status = ignorertHendelseStatus,
                statusTidspunkt = ZonedDateTime.now())
    }

    fun isAfter(tidspunkt: ZonedDateTime): Boolean = statusTidspunkt.isAfter(tidspunkt)

    fun erVarslet(): Boolean = status == varsletStatus

    fun harKommetUnderOppfolging(hendelsesTidspunkt: ZonedDateTime): Status {
        if(status == nyBrukerStatus) return skalVarsles(this, hendelsesTidspunkt)
        if(status == doneStatus) return skalVarsles(nyttVarsel(this), hendelsesTidspunkt)
        if(status == skalVarslesStatus) return skalVarsles(nyttVarsel(this), hendelsesTidspunkt)
        return this
    }

    fun funnetFodselsnummer(fodselsnummer: String): Status {
        return funnetFodselsnummer(this, fodselsnummer = fodselsnummer)
    }

    fun varsleBruker(
            varselPublisher: VarselPublisher
    ): Status {
        if(status != skalVarslesStatus) return this

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