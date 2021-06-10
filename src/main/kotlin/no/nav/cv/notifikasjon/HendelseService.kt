package no.nav.cv.notifikasjon

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Period
import java.time.ZonedDateTime

private val cutoffPeriod = Period.ofDays(2)

interface HendelseService {

    fun kommetUnderOppfolging(aktorId: String, hendelsesTidspunkt: ZonedDateTime)

    fun varsleBruker(aktorId: String, hendelsesTidspunkt: ZonedDateTime)

    fun blittFulgtOpp(aktorId: String, hendelsesTidspunkt: ZonedDateTime)

    fun harSettCv(aktorId: String, hendelsesTidspunkt: ZonedDateTime)

}


@Service
class Hendelser (
    private val repository: StatusRepository,
    private val varselPublisher: VarselPublisher,
    private val abTestSelector: ABTestSelector
) : HendelseService {

    private val log = LoggerFactory.getLogger(Hendelser::class.java)

    override fun kommetUnderOppfolging(aktorId: String, hendelsesTidspunkt: ZonedDateTime) {

        val cutoffTime = ZonedDateTime.now().minus(cutoffPeriod)
        if(hendelsesTidspunkt.isBefore(cutoffTime)) {
            log.debug("Oppfølgingsperiode: ${hendelsesTidspunkt} startert før cutoff-perioden ($cutoffPeriod). Ignorerer oppfølgingsstatus")
            return
        }

        val status = repository.finnSiste(aktorId)
        val nesteStatus = status.harKommetUnderOppfolging(hendelsesTidspunkt, abTestSelector)

        repository.lagre(nesteStatus)
    }

    override fun varsleBruker(aktorId: String, hendelsesTidspunkt: ZonedDateTime){
        val status = repository.finnSiste(aktorId)
        val nesteStatus = status.varsleBruker(varselPublisher)
        repository.lagre(nesteStatus)
    }

    override fun blittFulgtOpp(aktorId: String, hendelsesTidspunkt: ZonedDateTime){
        val status = repository.finnSiste(aktorId)
        val nesteStatus = status.blittFulgtOpp(hendelsesTidspunkt, varselPublisher)
        repository.lagre(nesteStatus)
    }

    override fun harSettCv(aktorId: String, hendelsesTidspunkt: ZonedDateTime) {

        val cutoffTime = ZonedDateTime.now().minus(cutoffPeriod)
        if(hendelsesTidspunkt.isBefore(cutoffTime)) {
            log.debug("CV endret (${hendelsesTidspunkt} før cutoff-perioden ($cutoffPeriod). Ignorerer endringer")
            return
        }

        val status = repository.finnSiste(aktorId)
        val nesteStatus = status.harSettCv(hendelsesTidspunkt, varselPublisher)
        repository.lagre(nesteStatus)
    }

}

