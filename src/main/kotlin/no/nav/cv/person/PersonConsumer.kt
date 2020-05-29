package no.nav.cv.person

import io.micronaut.configuration.kafka.annotation.KafkaListener
import io.micronaut.configuration.kafka.annotation.OffsetReset
import io.micronaut.configuration.kafka.annotation.Topic
import org.apache.avro.generic.GenericArray
import org.apache.avro.generic.GenericEnumSymbol
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory


@KafkaListener(
        groupId = "pam-brukernotifikasjon-person",
        offsetReset = OffsetReset.EARLIEST
)
class PersonConsumer(
        private val personIdentRepository: PersonIdentRepository
) {

    companion object {
        val log = LoggerFactory.getLogger(PersonConsumer::class.java)
    }

    @Topic("\${kafka.topics.consumers.pdl_id}")
    fun receive(
            record: ConsumerRecord<String, GenericRecord>
    ) {
        val identer = PersonDto(record.value())
                .identer()
        personIdentRepository.oppdater(identer)
    }

}


class PersonDto(val record: GenericRecord) {

    fun identer(): PersonIdenter {
        val identer = record.identifikatorer().map { PersonIdent(
                it.get("idnummer") as String,
                convertType(it.get("type") as GenericEnumSymbol<*>),
                it.get("gjeldende") as Boolean
        ) }
        return PersonIdenter(identer)
    }

    private fun convertType(type: GenericEnumSymbol<*>): PersonIdent.Type {
        when(type.toString()) {
            "FOLKEREGISTERIDENTIFIKATOR" -> return PersonIdent.Type.FOLKEREGISTER
            "AKTOR_ID" -> return PersonIdent.Type.AKTORID
            else -> return PersonIdent.Type.ANNET
        }
    }

    private fun GenericRecord.identifikatorer(): GenericArray<GenericRecord> =
            get("identifikatorer") as GenericArray<GenericRecord>
}

