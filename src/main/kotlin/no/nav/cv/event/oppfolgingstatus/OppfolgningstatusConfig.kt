package no.nav.cv.event.oppfolgingstatus

import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.context.annotation.Requires
import io.micronaut.context.annotation.Value
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Consumes
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Header
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client


@ConfigurationProperties("oppfolgingstatus")
@Requires(property = "oppfolgingstatus")
class OppfolgningstatusConfig(
        @Value("\${sts.host}") val securityTokenServiceHost: String,
        @Value("\${feed.host}") val oppfolgingsstatusFeedHost: String
) {

}

@Client("\${oppfolgingstatus.sts.host}")
interface SecurityTokeServiceClient {

    @Consumes(MediaType.APPLICATION_JSON)
    @Get("/rest/v1/sts/token")
    fun authenticate(@Header("Authorization") authorization: String): String

}

data class Token(
        val access_token: String,
        val token_type: String,
        val expires_in: String
)

@Client("\${oppfolgingstatus.feed.host}")
interface OppfolgingsStatusFeedClient {

    @Consumes(MediaType.TEXT_PLAIN)
    @Get("/veilarboppfolging/api/feed/oppfolging")
    fun feed(
            @Header authorization: String,
            @QueryValue("id") id: Long,
            @QueryValue("page_size") pageSize: Long): HttpResponse<String>

}