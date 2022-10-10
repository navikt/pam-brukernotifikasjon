package no.nav.cv.event.oppfolgingstatus

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

val dateString = "2021-05-26T12:23:30.794367+02:00";
val date = ZonedDateTime.parse(dateString, DateTimeFormatter.ISO_ZONED_DATE_TIME);

class OppfolgingSerializationTest {

    @Test
    fun testStartetDeserialzation() {
        val startedJson = """
            {
                "aktorId": "123",
                "oppfolgingStartet": "$dateString"
            }
        """.trimIndent()
        startedJson
                .let { Json.decodeFromString<OppfolgingStartet>(it) }
                .also { """"OppfolgingStartet record received for ${it.aktorId}."""" }
        val opppfolgingStartet = Json.decodeFromString<OppfolgingStartet>(startedJson)

        Assertions.assertEquals("123", opppfolgingStartet.aktorId)
        Assertions.assertEquals(date, opppfolgingStartet.oppfolgingStartet)
    }

    @Test
    fun testAvsluttetDeserialzation() {
        val avsluttetJson = """
            {
                "aktorId": "123",
                "sluttdato": "$dateString"
            }
        """.trimIndent()

        val oppfolgingAvsluttet = Json.decodeFromString<OppfolgingAvsluttet>(avsluttetJson)

        Assertions.assertEquals("123", oppfolgingAvsluttet.aktorId)
        Assertions.assertEquals(date, oppfolgingAvsluttet.sluttdato)
    }

}

