package no.nav.cv.notifikasjon

import io.micronaut.test.annotation.MicronautTest
import io.micronaut.test.annotation.MockBean
import io.mockk.mockk
import io.mockk.verify
import no.nav.cv.output.BesokCvOppgavePublisher
import org.junit.jupiter.api.Test
import javax.inject.Inject

// This gets rather close to an integration test - maybe it should be named accordingly?
@MicronautTest
class HendelseServiceTest {

    private val fnr = "dummy"

    @Inject
    lateinit var varselPublisher: VarselPublisher

    @Inject
    lateinit var hendelseService: HendelseService

    @MockBean(BesokCvOppgavePublisher::class)
    fun varselPublisher(): VarselPublisher = mockk(relaxed = true)

    @Test
    fun `ny bruker - kommet under oppfolging - varsles`() {
        hendelseService.kommetUnderOppfolging(fnr)
        verify(exactly = 1) { varselPublisher.publish(any(), fnr) }
        verify(exactly = 0) { varselPublisher.done(any(), any()) }
    }

    @Test
    fun `ny bruker - fullfort oppfolging - ingen varsler`() {
        hendelseService.erFulgtOpp(fnr)
        verify(exactly = 0) { varselPublisher.publish(any(), any()) }
        verify(exactly = 0) { varselPublisher.done(any(), any()) }
    }

    @Test
    fun `ny bruker - sett CV - ingen varsler`() {
        hendelseService.settCv(fnr)
        verify(exactly = 0) { varselPublisher.publish(any(), any()) }
        verify(exactly = 0) { varselPublisher.done(any(), any()) }
    }

    @Test
    fun `ny bruker - kommet under oppfolging - kommet under oppfolging igjen - varsles 1 gang`() {
        hendelseService.kommetUnderOppfolging(fnr)
        hendelseService.kommetUnderOppfolging(fnr)
        verify(exactly = 1) { varselPublisher.publish(any(), fnr) }
        verify(exactly = 0) { varselPublisher.done(any(), any()) }
    }

    @Test
    fun `ny bruker - kommet under oppfolging - fullfort oppfolging - varsles forst - sa done`() {
        hendelseService.kommetUnderOppfolging(fnr)
        hendelseService.erFulgtOpp(fnr)
        verify(exactly = 1) { varselPublisher.publish(any(), fnr) }
        verify(exactly = 1) { varselPublisher.done(any(), fnr) }
    }

    @Test
    fun `ny bruker - kommet under oppfolging - sett CV - varsles forst - sa done`() {
        hendelseService.kommetUnderOppfolging(fnr)
        hendelseService.settCv(fnr)
        verify(exactly = 1) { varselPublisher.publish(any(), fnr) }
        verify(exactly = 1) { varselPublisher.done(any(), fnr) }
    }

    @Test
    fun `ny bruker - kommet under oppfolging - sett CV - sa kommer under oppfoling igjen - varsles forst - sa done - sa varsles`() {
        hendelseService.kommetUnderOppfolging(fnr)
        hendelseService.settCv(fnr)
        hendelseService.kommetUnderOppfolging(fnr)
        verify(exactly = 2) { varselPublisher.publish(any(), fnr) }
        verify(exactly = 1) { varselPublisher.done(any(), fnr) }
    }


}