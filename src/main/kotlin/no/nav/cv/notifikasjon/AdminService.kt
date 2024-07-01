package no.nav.cv.notifikasjon

import no.nav.cv.output.OutboxService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminService(
    private val statusRepository: StatusRepository,
    private val outboxService: OutboxService
) {
    companion object {
        private val log = LoggerFactory.getLogger(NotifikasjonAdmin::class.java)
    }

    @Transactional
    fun lukkAlleÅpneVarsler() {
        val åpneVarlser = statusRepository.finnAlleMedÅpneVarsler()

        log.info("Fant ${åpneVarlser.size} åpne varlser")

        åpneVarlser.forEach {
            statusRepository.settFerdig(it.aktoerId)
            outboxService.schdeuleDone(it.uuid, it.aktoerId, it.fnr)
        }

        log.info("Markerte ${åpneVarlser.size} statuser med varslet som ferdig og skedulerte DONE-melding")
    }
}
