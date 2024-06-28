package no.nav.cv.infrastructure.health

import no.nav.cv.infrastructure.kafka.ConsumerStatusHandler
import no.nav.security.token.support.core.api.Unprotected
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal")
@Unprotected
class ApplicationStatusController(
    private val consumerStatusHandler: ConsumerStatusHandler
) {
    private val log = LoggerFactory.getLogger(ApplicationStatusController::class.java)

    @GetMapping("/isReady")
    fun isReady() = "Ready for everything"

    @GetMapping("/isAlive")
    fun isAlive(): ResponseEntity<String> {
        return ResponseEntity.ok("OK")
    }

}
