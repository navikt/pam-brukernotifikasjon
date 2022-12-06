package no.nav.cv.event.oppfolgingstatus

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.ZonedDateTime

@Serializable
data class OppfolgingStartet (
    val aktorId: String,

    @Serializable(KZonedDateTimeSerializer::class)
    val oppfolgingStartet: ZonedDateTime
)

@Serializable
data class OppfolgingAvsluttet (
    val aktorId: String,

    @Serializable(KZonedDateTimeSerializer::class)
    val sluttdato: ZonedDateTime
)

object KZonedDateTimeSerializer : KSerializer<ZonedDateTime> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ZonedDateTime", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ZonedDateTime) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): ZonedDateTime {
        val string = decoder.decodeString()
        return ZonedDateTime.parse(string)
    }
}
