package no.nav.cv.notifikasjon

import java.time.LocalDateTime
import java.util.*

val ukjentStatus = "ukjent"
val varsletStatus = "varslet"
val doneStatus = "done"
val ignorertHendelseStatus = "ignorert"

class Status(
        val uuid: UUID,
        val fnr: String,
        val status: String,
        val statusTidspunkt: LocalDateTime
) {

    companion object {
        fun varslet(uuid: UUID, fnr: String, statusTidspunkt: LocalDateTime) =
                Status(uuid, fnr, varsletStatus, statusTidspunkt)
        fun done(uuid: UUID, fnr: String, statusTidspunkt: LocalDateTime) =
                Status(uuid, fnr, doneStatus, statusTidspunkt)
        fun ukjent(fnr: String) =
                Status(UUID.randomUUID(), fnr, ukjentStatus, LocalDateTime.now())
        fun ignorert(fnr: String) =
                Status(UUID.randomUUID(), fnr, ignorertHendelseStatus, LocalDateTime.now())
    }

    fun isAfter(tidspunkt: LocalDateTime): Boolean = statusTidspunkt.isAfter(tidspunkt)

    fun erVarslet(): Boolean = status == varsletStatus

}
