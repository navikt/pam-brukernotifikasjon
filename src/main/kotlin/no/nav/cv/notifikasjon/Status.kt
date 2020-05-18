package no.nav.cv.notifikasjon

import java.time.ZonedDateTime
import java.util.*

val ukjentStatus = "ukjent"
val varsletStatus = "varslet"
val doneStatus = "done"
val ignorertHendelseStatus = "ignorert"
private val startOfTime = ZonedDateTime.now().minusYears(40)

class Status(
        val uuid: UUID,
        val fnr: String,
        val status: String,
        val statusTidspunkt: ZonedDateTime
) {

    companion object {
        fun ukjent(fnr: String) =
                Status(UUID.randomUUID(), fnr, ukjentStatus, startOfTime)
        fun skalVarsles(uuid: UUID, fnr: String, statusTidspunkt: ZonedDateTime) =
            Status(uuid, fnr, varsletStatus, statusTidspunkt)
        fun varslet(uuid: UUID, fnr: String, statusTidspunkt: ZonedDateTime) =
                Status(uuid, fnr, varsletStatus, statusTidspunkt)
        fun varselFjernes(uuid: UUID, fnr: String, statusTidspunkt: ZonedDateTime) = // TODO Avgjøre om dette er samme som done?
                Status(uuid, fnr, varsletStatus, statusTidspunkt)
        fun done(uuid: UUID, fnr: String, statusTidspunkt: ZonedDateTime) =
                Status(uuid, fnr, doneStatus, statusTidspunkt)

        fun ignorert(fnr: String) =
                Status(UUID.randomUUID(), fnr, ignorertHendelseStatus, ZonedDateTime.now())
    }

    fun isAfter(tidspunkt: ZonedDateTime): Boolean = statusTidspunkt.isAfter(tidspunkt)

    fun erVarslet(): Boolean = status == varsletStatus

}
