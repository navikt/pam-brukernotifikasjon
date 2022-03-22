package no.nav.cv.infrastructure.metrics

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import no.nav.cv.notifikasjon.StatusRepository
import no.nav.security.token.support.core.api.Unprotected
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal/metrics")
@Unprotected
class ConversionRateController(
    private val statusRepository: StatusRepository,
    @Value("\${metrics.key.conversion_rate}") private val key: String,
) {

    private val log = LoggerFactory.getLogger(ConversionRateController::class.java)

    @OptIn(ExperimentalSerializationApi::class)
    @GetMapping("/conversionRate", produces = ["application/json"])
    fun getConversionRate(
        @RequestHeader("Authentication") reqKey: String
    ) = if (reqKey == key)
        statusRepository
            .conversionRateWeekly()
            .also { log.info("Conversion rate endpoint queried. Returning ${it.size} rows.") }
            .let { Json.encodeToString(it) }
    else throw Exception("Bad auth")

}