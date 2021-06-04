package no.nav.cv.infrastructure.health

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller("internal")
class ApplicationStatusController {

    @GetMapping("isReady")
    fun isReady() = "Ready for everything"

    @GetMapping("isAlive")
    fun isAlive() = "Alive and kicking"

}
