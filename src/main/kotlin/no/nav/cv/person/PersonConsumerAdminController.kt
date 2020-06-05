package no.nav.cv.person

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post

@Controller("internal/pdl/kafka/admin")
class PersonConsumerAdminController(
        private val personConsumer: PersonConsumer
) {

    @Post("reset")
    fun resetTopic() = personConsumer.reset()

}