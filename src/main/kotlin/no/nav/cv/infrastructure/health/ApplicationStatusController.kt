package no.nav.cv.infrastructure.health

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("internal")
class ApplicationStatusController {

    @GetMapping("isReady")
    fun isReady() = "Ready for everything"

    @GetMapping("isAlive")
    fun isAlive() = "Alive and kicking"

}
