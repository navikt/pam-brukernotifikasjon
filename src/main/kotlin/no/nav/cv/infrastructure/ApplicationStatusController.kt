package no.nav.cv.infrastructure

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import org.slf4j.LoggerFactory


@Controller("internal")
class ApplicationStatusController {

    @Get("isReady")
    fun isReady() = "Ready for everything"

    @Get("isAlive")
    fun isAlive() = "Alive and kicking"

}