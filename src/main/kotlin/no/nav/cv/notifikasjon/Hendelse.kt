package no.nav.cv.notifikasjon

import java.time.ZonedDateTime
import java.util.*

private val kommetUnderOppfolging = "kommet under oppfølgning"
private val erFulgtOpp = "er fulgt opp"
private val settCv = "sett CV"

class Hendelse private constructor(
        val uuid: UUID,
        val fnr: String,
        val eventType: String,
        val hendelseTidspunkt: ZonedDateTime,
        val nyesteStatus: Status
) {

    companion object {
        fun kommetUnderOppfolging(fnr: String, nyesteStatus: Status, timestamp: ZonedDateTime): Hendelse =
                Hendelse(nyesteStatus.uuid, fnr, kommetUnderOppfolging, timestamp, nyesteStatus)

        fun erFulgtOpp(fnr: String, nyesteStatus: Status, timestamp: ZonedDateTime): Hendelse =
                Hendelse(nyesteStatus.uuid, fnr, erFulgtOpp, timestamp, nyesteStatus)

        fun settCv(fnr: String, nyesteStatus: Status, timestamp: ZonedDateTime): Hendelse =
                Hendelse(nyesteStatus.uuid, fnr, settCv, timestamp, nyesteStatus)

    }

    fun produserVarsel(): Varsel {
        if(nyesteStatus.isAfter(hendelseTidspunkt))
            throw IllegalStateException("Feil i tidslinje")

        if(!nyesteStatus.erVarslet() && eventType == kommetUnderOppfolging)
            return Varsel.varsles(uuid, fnr, hendelseTidspunkt)

        if(nyesteStatus.erVarslet() && eventType == erFulgtOpp)
            return Varsel.fullfortOppgave(uuid, fnr, hendelseTidspunkt)

        if(nyesteStatus.erVarslet() && eventType == settCv)
            return Varsel.fullfortOppgave(uuid, fnr, hendelseTidspunkt)

        return Varsel.ikkeVarsle(uuid, fnr, hendelseTidspunkt)
    }


}