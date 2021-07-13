package no.nav.cv.personident

import no.nav.security.token.support.client.core.ClientProperties
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.function.Supplier

@Configuration
@EnableOAuth2Client(cacheEnabled = true)
class IdentOppslagConfiguration {

    @Bean("personoppslagTokenProvider")
    fun tokenProvider(
        clientConfigurationProperties: ClientConfigurationProperties,
        oAuth2AccessTokenService: OAuth2AccessTokenService
    ): Supplier<String> {
        val clientProperties: ClientProperties = clientConfigurationProperties.registration["personoppslag"]
            ?: throw RuntimeException("could not find oauth2 client config for personoppslag")

        return Supplier { oAuth2AccessTokenService.getAccessToken(clientProperties).accessToken }
    }

}