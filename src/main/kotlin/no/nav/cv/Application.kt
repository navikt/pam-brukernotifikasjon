package no.nav.cv

import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling


@SpringBootApplication
@EnableSchedulerLock(defaultLockAtMostFor = "5m")
@EnableScheduling
//@EnableJwtTokenValidation(ignore = ["no.nav.cv.infrastructure.health","org.springframework"])
//@EnableJwtTokenValidation
class PamBrukernotifikasjonApplication

fun main(args: Array<String>) {
    runApplication<PamBrukernotifikasjonApplication>(*args)
}

@Configuration
class JwtBean {
    @Bean
    fun oidcRequestContextHolder(): TokenValidationContextHolder {
        return SpringTokenValidationContextHolder()
    }
}