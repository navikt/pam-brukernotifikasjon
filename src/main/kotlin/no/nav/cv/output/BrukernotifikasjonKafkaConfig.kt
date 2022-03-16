package no.nav.cv.output

import no.nav.brukernotifikasjon.schemas.input.DoneInput
import no.nav.brukernotifikasjon.schemas.input.NokkelInput
import no.nav.brukernotifikasjon.schemas.input.OppgaveInput
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.producer.KafkaProducer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*

@Configuration
class BrukernotifikasjonKafkaConfig {

    private val log = LoggerFactory.getLogger(BrukernotifikasjonKafkaConfig::class.java)


    @Bean
    fun doneKafkaProducer(
            @Qualifier("defaultProducerProperties") props: Properties
    ) : KafkaProducer<NokkelInput, DoneInput> {
        props[CommonClientConfigs.CLIENT_ID_CONFIG] = "pam-brukernotifikasjon-done-producer"
        return KafkaProducer<NokkelInput, DoneInput>(props)
    }

    @Bean
    fun oppgaveKafkaProducer(
            @Qualifier("defaultProducerProperties") props: Properties
    ) : KafkaProducer<NokkelInput, OppgaveInput> {

        props[CommonClientConfigs.CLIENT_ID_CONFIG] = "pam-brukernotifikasjon-oppgave-producer"
        return KafkaProducer<NokkelInput, OppgaveInput>(props)
    }


}

