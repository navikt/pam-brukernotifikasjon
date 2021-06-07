package no.nav.cv.person

import no.nav.cv.infrastructure.kafka.EventProcessor
import org.apache.avro.generic.GenericArray
import org.apache.avro.generic.GenericEnumSymbol
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PersonEndretProcessor(
        private val personIdentRepository: PersonIdentRepository
) : EventProcessor<String, GenericRecord> {

    companion object {
        val log = LoggerFactory.getLogger(PersonEndretProcessor::class.java)
    }

    fun receive(
            record: ConsumerRecord<String, GenericRecord>
    ) {
        log.debug("PersonConsumer record recieved ${record.key()}")
        val identer = PersonDto(record.value())
                .identer()
        personIdentRepository.oppdater(identer)
    }


    override suspend fun process(records: ConsumerRecords<String, GenericRecord>) {
        records.forEach { receive(it) }
    }

}


class PersonDto(val record: GenericRecord) {

    fun identer(): PersonIdenter {
        val identer = record.identifikatorer().map { PersonIdent(
                it.get("idnummer").toString(),
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

    @Suppress("UNCHECKED_CAST")
    private fun GenericRecord.identifikatorer(): GenericArray<GenericRecord> =
            get("identifikatorer") as GenericArray<GenericRecord>
}

