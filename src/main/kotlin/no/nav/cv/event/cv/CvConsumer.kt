package no.nav.cv.event.cv

import io.micronaut.configuration.kafka.annotation.KafkaListener
import io.micronaut.configuration.kafka.annotation.OffsetReset
import io.micronaut.configuration.kafka.annotation.Topic
import no.nav.cv.notifikasjon.HendelseService
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.ZonedDateTime
import java.util.*


@KafkaListener(
        groupId = "pam-brukernotifikasjon-cv",
        offsetReset = OffsetReset.EARLIEST
)
class CvConsumer(
        val hendelseService: HendelseService
) {

    companion object {
        val log = LoggerFactory.getLogger(CvConsumer::class.java)
    }

    @Topic("\${kafka.topics.consumers.cv_endret}")
    fun receive(
            record: ConsumerRecord<String, GenericRecord>
    ) {
        log.debug("CvConsumer record recieved: $record")

        val cv = CvDto(record.value())
        log.info("${cv.aktorId()}: ${cv.sistEndret()}")

        hendelseService.harSettCv(cv.aktorId(), cv.sistEndret())
        log.info("CV ${record.key()}, AktoerId: ${cv.aktorId()}, Sist endret: ${cv.sistEndret()}")
    }

}

private class CvDto(val record: GenericRecord) {

    val opprett = "OPPRETT"
    val endre = "ENDRE"
    val slett = "SLETT"

    fun aktorId(): String {

        val (cv, jobbprofil) = getCvJobprofil()

        return cv?.aktoerId()
                ?: jobbprofil?.aktoerId()
                ?: throw IllegalStateException("Mangler aktørid")
    }

    fun sistEndret() =
            ZonedDateTime.ofInstant(Instant.ofEpochMilli(sistEndretMillis()), TimeZone.getDefault().toZoneId())

    private fun sistEndretMillis(): Long {

        val (cv, jobbprofil) = getCvJobprofil()

        return cv?.sistEndret()
                ?: jobbprofil?.sistEndret()
                ?: throw IllegalStateException("Mangler sistEndret")
    }

    private fun getCvJobprofil()
            = when(record.meldingstype()) {

            opprett -> Pair(record.opprettCv()?.cv(), record.opprettJobbprofil()?.jobbprofil())

            endre -> Pair(record.endretCv()?.cv(), record.endretJobbprofil()?.jobbprofil())

            slett -> Pair(record.slettCv()?.cv(), record.slettJobbprofil()?.jobbprofil())

            else -> throw IllegalStateException("Ukjent meldingstype")
        }
}


private fun GenericRecord.meldingstype() = get("meldingstype")?.toString() ?: "NULL"

private fun GenericRecord.opprettCv() = get("opprett_cv") as GenericRecord?

private fun GenericRecord.endretCv() = get("endre_cv") as GenericRecord?

private fun GenericRecord.slettCv() = get("slett_cv") as GenericRecord?

private fun GenericRecord.opprettJobbprofil() = get("opprett_jobbprofil") as GenericRecord?

private fun GenericRecord.endretJobbprofil() = get("endre_jobbprofil") as GenericRecord?

private fun GenericRecord.slettJobbprofil() = get("slett_jobbprofil") as GenericRecord?

private fun GenericRecord.cv() = get("cv") as GenericRecord?

private fun GenericRecord.jobbprofil() = get("jobbprofil") as GenericRecord?

private fun GenericRecord.sistEndret(): Long? = get("sist_endret") as Long?

private fun GenericRecord.aktoerId() = get("aktoer_id") as String?
