package no.nav.cv.notifikasjon

import java.time.ZonedDateTime
import java.util.*


private val varsles = "Bruker varsles"
private val fullfortOppgave = "Oppgave fullført"
private val ikkeVarsle = "Ikke varsle"

class Varsel private constructor(
        val uuid: UUID,
        val fnr: String,
        val type: String,
        val timestamp: ZonedDateTime
) {

    companion object {
        fun varsles(uuid: UUID, fnr: String, timestamp: ZonedDateTime) =
                Varsel(uuid, fnr, varsles, timestamp)
        fun fullfortOppgave(uuid: UUID, fnr: String, timestamp: ZonedDateTime) =
                Varsel(uuid, fnr, fullfortOppgave, timestamp)
        fun ikkeVarsle(uuid: UUID, fnr: String, timestamp: ZonedDateTime) =
                Varsel(uuid, fnr, ikkeVarsle, timestamp)

    }


    fun varsle(varselPublisher: VarselPublisher): Status {

        if(type == varsles) {
            varselPublisher.publish(uuid, fnr)
            return Status.varslet(uuid, fnr, timestamp)
        }

        if(type == fullfortOppgave) {
            varselPublisher.done(uuid, fnr)
            return Status.done(uuid, fnr, timestamp)
        }

        return Status.ignorert(fnr)
    }
}

