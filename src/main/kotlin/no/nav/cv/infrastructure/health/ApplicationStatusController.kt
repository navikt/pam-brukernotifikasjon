package no.nav.cv.infrastructure.health

import no.nav.cv.infrastructure.kafka.ConsumerStatusHandler
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Unprotected
@RequestMapping("internal")
class ApplicationStatusController(
    private val consumerStatusHandler: ConsumerStatusHandler
) {

    @GetMapping("isReady")
    fun isReady() = "Ready for everything"

    @GetMapping("isAlive")
    fun isAlive() = !consumerStatusHandler.isUnhealthy()

}
