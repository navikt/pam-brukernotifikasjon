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
    fun hentÅpneVarsler() = statusRepository.finnAlleMedÅpneVarsler()

    @Transactional
    fun lukkVarsel(varsel: Status) {
        outboxService.schdeuleDone(varsel.uuid, varsel.aktoerId, varsel.fnr)
        statusRepository.settFerdig(varsel.aktoerId)
    }
}
