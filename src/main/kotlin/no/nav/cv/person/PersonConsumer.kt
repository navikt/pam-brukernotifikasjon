package no.nav.cv.person

import io.micronaut.configuration.kafka.ConsumerAware
import io.micronaut.configuration.kafka.annotation.KafkaListener
import io.micronaut.configuration.kafka.annotation.OffsetReset
import io.micronaut.configuration.kafka.annotation.Topic
import io.micronaut.context.annotation.Prototype
import io.micronaut.context.annotation.Value
import org.apache.avro.generic.GenericArray
import org.apache.avro.generic.GenericEnumSymbol
import org.apache.avro.generic.GenericRecord
import org.apache.avro.util.Utf8
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.PartitionInfo
import org.slf4j.LoggerFactory

@Prototype
@KafkaListener(
        groupId = "pam-brukernotifikasjon-person",
        offsetReset = OffsetReset.EARLIEST
)
class PersonConsumer(
        private val personIdentRepository: PersonIdentRepository
) : ConsumerAware<Any, Any> {

    lateinit var consumer: Consumer<Any, Any>

    companion object {
        val log = LoggerFactory.getLogger(PersonConsumer::class.java)
    }

    @Topic("\${kafka.topics.consumers.pdl_id}")
    fun receive(
            record: ConsumerRecord<String, GenericRecord>
    ) {
        log.debug("PersonConsumer record recieved: $record")
        val identer = PersonDto(record.value())
                .identer()
        personIdentRepository.oppdater(identer)
    }

    fun reset() = consumer.seekToBeginning(consumer.assignment())

    override fun setKafkaConsumer(consumer: Consumer<Any, Any>) {
        this.consumer = consumer
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

    private fun GenericRecord.identifikatorer(): GenericArray<GenericRecord> =
            get("identifikatorer") as GenericArray<GenericRecord>
}

