package no.nav.cv.notifikasjon

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.slot
import no.nav.cv.SingletonPostgresTestInstance
import no.nav.cv.output.OutboxService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.TestPropertySource
import java.util.*

@TestPropertySource(properties = ["schedulingEnabled=false"])
internal class OutboxTestInstance : SingletonPostgresTestInstance() {

    @Autowired
    private lateinit var outboxService: OutboxService

    @MockkBean
    private lateinit var varselPublisher : VarselPublisher

    private val aktorId = "aid123"
    private val fnr = "12345678901"

    @Test
    fun `schedule varsel - being sent`() {
        val uuid = UUID.randomUUID()

        val uuidSlot = slot<String>()
        val fnrSlot = slot<String>()

        every { varselPublisher.publish(capture(uuidSlot), capture(fnrSlot)) } returns Unit

        outboxService.schdeuleVarsel(uuid, aktorId, fnr)

        outboxService.processQueue(noDelay = true)

        assertEquals(uuid.toString(), uuidSlot.captured)
        assertEquals(fnr, fnrSlot.captured)
    }

    @Test
    fun `schedule done - being sent`() {
        val uuid = UUID.randomUUID()

        val uuidSlot = slot<String>()
        val fnrSlot = slot<String>()

        every { varselPublisher.done(capture(uuidSlot), capture(fnrSlot)) } returns Unit

        outboxService.schdeuleDone(uuid, aktorId, fnr)

        outboxService.processQueue(noDelay = true)

        assertEquals(uuid.toString(), uuidSlot.captured)
        assertEquals(fnr, fnrSlot.captured)
    }

}
