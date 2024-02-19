package no.nav.cv.personident

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URI
import java.util.function.Supplier
import javax.inject.Named

@Service
class PersonOppslag (
    @Value("\${base.url.personoppslag}") personoppslagBaseUrl: String,
    @Named("personoppslagTokenProvider") private val personOppslagTokenProvider: Supplier<String>
) {
    companion object {
        private val log = LoggerFactory.getLogger(PersonOppslag::class.java)
    }
    private val url = "${personoppslagBaseUrl}/pam-personoppslag/personidenter/system/oppslag/"

    fun hentAktorId(fnr: String): String? = personOppslag(fnr).aktorId

    fun hentFoedselsnummer(aktorId: String): String? = personOppslag(aktorId).foedselsnummer


    private fun personOppslag(
        ident: String
    ): PersonIdentResponseDto {
        val oppslagUrl = url + ident
        val (responseCode, responseBody) = with(URI(oppslagUrl).toURL().openConnection() as HttpURLConnection) {
            requestMethod = "GET"
            connectTimeout = 10000
            readTimeout = 10000

            setRequestProperty("Authorization", "Bearer ${personOppslagTokenProvider.get()}")
            setRequestProperty("Accept", "application/json")

            val stream: InputStream? = if (responseCode < 300) this.inputStream else this.errorStream
            responseCode to stream?.use { s-> s.bufferedReader().readText() }
        }

        if (responseCode >= 300 || responseBody == null) {
            log.error("Fikk feil fra personoppslag ($ident): $responseBody - body: $responseBody")
            return PersonIdentResponseDto.empty
        }

        return PersonIdentResponseDto.create(responseBody)
    }


}

@Serializable
private data class PersonIdentResponseDto(
    val foedselsnummer : String? = null,
    val aktorId : String? = null,
    val kandidatnummer : String? = null,
    val arenaKandidatnummer : String?  = null
) {

    companion object {

        val empty = PersonIdentResponseDto()

        fun create(json: String) = Json.decodeFromString<PersonIdentResponseDto>(json)

    }
}
