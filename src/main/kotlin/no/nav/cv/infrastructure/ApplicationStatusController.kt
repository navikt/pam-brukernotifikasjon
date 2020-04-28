package no.nav.cv.infrastructure

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get


@Controller("internal")
class ApplicationStatusController {

    @Get("isReady")
    fun isReady() = "Ready for everything"

    @Get("isAlive")
    fun isAlive() = "Alive and kicking"

}