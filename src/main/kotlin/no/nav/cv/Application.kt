package no.nav.cv

import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableSchedulerLock(defaultLockAtMostFor = "5m")
@EnableScheduling
@EnableJwtTokenValidation
class PamBrukernotifikasjonApplication

fun main(args: Array<String>) {
    runApplication<PamBrukernotifikasjonApplication>(*args)
}
