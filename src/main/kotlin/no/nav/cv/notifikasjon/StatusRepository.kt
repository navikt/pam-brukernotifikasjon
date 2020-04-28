package no.nav.cv.notifikasjon

interface StatusRepository {

    fun lagre(status: Status)

    fun finnSiste(fnr: String): Status

}