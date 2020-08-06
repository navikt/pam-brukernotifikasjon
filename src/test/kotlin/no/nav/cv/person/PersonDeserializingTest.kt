package no.nav.cv.person

import org.apache.avro.Protocol
import org.apache.avro.Schema
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import java.nio.file.Paths

class PersonDeserializingTest {

    val protocol = Protocol.parse(avroFile())
    val aktorSchema = protocol.getType("Aktor")
    val relasjonSchema = protocol.getType("Relasjon")
    val identifikatorSchema = protocol.getType("Identifikator")
    val typeSchema = protocol.getType("Type")

    //@Disabled("Unnecessary - used to test generation of testdata using schema")
    @Test
    fun `verify schema verification`() {

        val personIdenter = genererPerson(listOf(
                fnr("123", true),
                aktorId("234", false)
        ))


        assertTrue(GenericData.get().validate(aktorSchema, personIdenter))
    }

    @Test
    fun `test deserializing`() {

        val personIdenter = genererPerson(listOf(
                fnr("123", true),
                aktorId("234", false)
        ))
        val identer = PersonDto(personIdenter).identer()

        assertAll(
                Executable { assertTrue(identer.identer().containsAll(listOf(
                        PersonIdent("123", PersonIdent.Type.FOLKEREGISTER, true),
                        PersonIdent("234", PersonIdent.Type.AKTORID, false)

                ))) }
        )

    }

    fun genererPerson(identifikatorer: List<GenericRecord>): GenericRecord {
        val record = GenericData.Record(aktorSchema)
        record.put("identifikatorer", GenericData.Array(Schema.createArray(identifikatorSchema), identifikatorer))
        record.put("relasjoner", GenericData.Array<GenericRecord>(Schema.createArray(relasjonSchema), listOf()))
        return record
    }

    fun fnr(idnummer: String, gjeldende: Boolean): GenericRecord =
            identifikatorer(idnummer, GenericData.EnumSymbol(typeSchema, "FOLKEREGISTERIDENTIFIKATOR"), gjeldende)
    fun aktorId(idnummer: String, gjeldende: Boolean): GenericRecord =
            identifikatorer(idnummer, GenericData.EnumSymbol(typeSchema, "AKTOR_ID"), gjeldende)

    fun identifikatorer(idnummer: String, type: GenericData.EnumSymbol, gjeldende: Boolean): GenericRecord {
        val id = GenericData.Record(identifikatorSchema)
        id.put("idnummer", idnummer)
        id.put("type", type)
        id.put("gjeldende", gjeldende)
        return id
    }


    private fun avroFile() = Paths.get(PersonDeserializingTest::class.java.getResource("/pdl.avpr").toURI()).toFile()

}