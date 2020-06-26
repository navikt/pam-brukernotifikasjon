package no.nav.cv.event.oppfolgingstatus

import io.micronaut.context.annotation.Value
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Consumes
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Header
import io.micronaut.http.client.annotation.Client
import org.json.JSONObject
import java.util.*
import javax.inject.Singleton

private val uninitialized = "uninitialized"

@Singleton
class TokenProvider(
        private val securityTokeServiceClient: SecurityTokeServiceClient,
        @Value("\${oppfolgingstatus.sts.serviceuser.username}") val user: String,
        @Value("\${oppfolgingstatus.sts.serviceuser.password}") val pw: String
) {

    private var oidcToken: String = uninitialized

    fun get() : String {
        // TODO consider to handle that we get close to expiry
        if(oidcToken == uninitialized) refresh()

        return oidcToken
    }

    fun refresh() {
        oidcToken = fetchToken()
    }

    private fun fetchToken(): String {
        OppfolgingsstatusRest.log.debug("Fetching token")
        val auth = Base64.getEncoder().encodeToString("$user:$pw".toByteArray(Charsets.UTF_8))
        val response = securityTokeServiceClient.authenticate("Basic $auth")

        JSONObject(response).query("/access_token")
        return JSONObject(response).getString("access_token") ?: uninitialized
    }
}

@Client("\${oppfolgingstatus.sts.host}")
interface SecurityTokeServiceClient {

    @Consumes(MediaType.APPLICATION_JSON)
    @Get("/rest/v1/sts/token")
    fun authenticate(@Header("Authorization") authorization: String): String

}