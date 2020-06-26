package no.nav.cv.event.oppfolgingstatus

import no.nav.cv.notifikasjon.HendelseService
import org.json.JSONObject
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.Period
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAmount
import javax.inject.Singleton

@Singleton
class OppfolgingstatusService (
        private val hendelseService: HendelseService
) {

    companion object {
        val log = LoggerFactory.getLogger(OppfolgingstatusService::class.java)
    }

    fun oppdaterStatus(dto: OppfolgingstatusDto) {
        if(dto.underOppfolging()) {
            hendelseService.kommetUnderOppfolging(dto.aktorId(), dto.startForSisteOppfolgningsPeriode())
        } else {
            hendelseService.blittFulgtOpp(dto.aktorId(), dto.endret())
        }
    }
}


class OppfolgingstatusDto(
        json: String
) {
    private val json: JSONObject = JSONObject(json)

    fun aktorId() = json.getString("aktoerid")

    fun underOppfolging() = json.getBoolean("oppfolging")

    fun startForSisteOppfolgningsPeriode() = ZonedDateTime.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(json.getString("startDato")))

    fun endret() = ZonedDateTime.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(json.getString("endretTimestamp")))

}