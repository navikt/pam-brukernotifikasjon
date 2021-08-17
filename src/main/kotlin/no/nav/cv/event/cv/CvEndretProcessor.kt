package no.nav.cv.event.cv

import no.nav.cv.infrastructure.kafka.EventProcessor
import no.nav.cv.notifikasjon.HendelseService
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.ZonedDateTime
import java.util.*

@Service
class CvEndretProcessor (
        val hendelseService: HendelseService
) : EventProcessor<String, GenericRecord> {

    companion object {
        val log = LoggerFactory.getLogger(CvEndretProcessor::class.java)
    }

    fun receive(
            record: ConsumerRecord<String, GenericRecord>
    ) {
        val cv = CvDto(record.value())

        if(!cv.slettetCv() && cv.endretAvBruker()) {
            log.info("Kafka CV msg for ${cv.aktorId()} is NOT a deletion msg and was initiated by user")
            hendelseService.endretCV(cv.aktorId(), cv.sistEndret())
        } else {
            log.info("Kafka CV msg for ${cv.aktorId()} is a deletion msg, so skipping it")
        }

    //log.info("CV ${record.key()}, Sist endret: ${cv.sistEndret()}")
    }

    override suspend fun process(records: ConsumerRecords<String, GenericRecord>) {
        records.forEach { receive(it) }
    }

}

private class CvDto(val record: GenericRecord) {
    fun aktorId() = record.aktoerId()
            ?: throw Exception("Record mangler aktorId")

    fun sistEndret() =
            ZonedDateTime.ofInstant(Instant.ofEpochMilli(sistEndretMillis()), TimeZone.getDefault().toZoneId())

    fun slettetCv() = record.get("meldingstype")?.toString()?.equals("SLETT") == true

    /**
     * This function will return true if the 'endret_av' value doesn't exist in the record and if the
     * value exists and equals 'PERSONBRUKER' in order to keep changes backwards compatible.
    */
    fun endretAvBruker() = record.get("endret_av")?.toString()?.equals("PERSONBRUKER") != false

    private fun sistEndretMillis() = record.sistEndret()
            ?: throw Exception("Record mangler sistEndret")
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
