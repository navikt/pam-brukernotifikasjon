package no.nav.cv.notifikasjon

import no.nav.cv.person.PersonIdent
import no.nav.cv.person.PersonIdentRepository
import java.time.ZonedDateTime
import java.util.*

val ukjentFnr = "ukjent"
val nyBrukerStatus = "ukjent"
val skalVarslesStatus = "skalVarsles"
val varsletStatus = "varslet"
val doneStatus = "done"
val ignorertHendelseStatus = "ignorert"
private val startOfTime = ZonedDateTime.now().minusYears(40)

class Status(
        val uuid: UUID,
        val aktorId: String,
        val fnr: String = ukjentFnr,
        val status: String,
        val statusTidspunkt: ZonedDateTime,
        val fortsettTidspunkt: ZonedDateTime = statusTidspunkt
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

        fun skalVarsles(forrigeStatus: Status, statusTidspunkt: ZonedDateTime, fortsettTidspunkt: ZonedDateTime) = Status(
                uuid = forrigeStatus.uuid,
                aktorId = forrigeStatus.aktorId,
                fnr = forrigeStatus.fnr,
                status = skalVarslesStatus,
                statusTidspunkt = statusTidspunkt,
                fortsettTidspunkt = fortsettTidspunkt)

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

        fun retry(forrigeStatus: Status) = Status(
                uuid = forrigeStatus.uuid,
                aktorId = forrigeStatus.aktorId,
                fnr = forrigeStatus.fnr,
                status = forrigeStatus.status,
                statusTidspunkt = forrigeStatus.statusTidspunkt,
                fortsettTidspunkt = forrigeStatus.fortsettTidspunkt.plusMinutes(1))

        fun ignorert(aktorId: String) = Status(
                uuid = UUID.randomUUID(),
                aktorId = aktorId,
                status = ignorertHendelseStatus,
                statusTidspunkt = ZonedDateTime.now())
    }

    fun isAfter(tidspunkt: ZonedDateTime): Boolean = statusTidspunkt.isAfter(tidspunkt)

    fun erVarslet(): Boolean = status == varsletStatus

    fun harKommetUnderOppfolging(hendelsesTidspunkt: ZonedDateTime): Status {
        if(status == nyBrukerStatus) return skalVarsles(this, hendelsesTidspunkt, ZonedDateTime.now())
        if(status == doneStatus) return skalVarsles(nyttVarsel(this), hendelsesTidspunkt, ZonedDateTime.now())
        if(status == skalVarslesStatus) return skalVarsles(nyttVarsel(this), hendelsesTidspunkt, ZonedDateTime.now())
        return this
    }

    fun varsleBruker(
            hendelsesTidspunkt: ZonedDateTime,
            personIdentRepository: PersonIdentRepository,
            varselPublisher: VarselPublisher
    ): Status {
        if(status != skalVarslesStatus) return this

        val fnr = personIdentRepository.finnIdenter(aktorId).gjeldende(PersonIdent.Type.FOLKEREGISTER)

        return if(fnr != null) {
            varselPublisher.publish(uuid, fnr)
            varslet(this, fnr, ZonedDateTime.now())
        } else {
            retry(this)
        }
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

}