package no.nav.cv.event.oppfolgingstatus

import kotlinx.serialization.*
import java.time.ZonedDateTime

@Serializable
data class OppfolgingStartet (
    val aktorId: String,
    val oppfolgingStartet: ZonedDateTime
)

@Serializable
data class OppfolgingAvsluttet (
    val aktorId: String,
    val sluttDato: ZonedDateTime
)