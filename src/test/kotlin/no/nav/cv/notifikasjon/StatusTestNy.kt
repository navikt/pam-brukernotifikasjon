package no.nav.cv.notifikasjon


import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import no.nav.cv.personident.PersonOppslag
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.random.Random

@SpringBootTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@ExtendWith(SpringExtension::class)
internal class StatusTestNy {

    private val rnd = Random(42)
    var aktoer = "will be replaced"
    var aktoerFnr = "will be replaced"
    val usedIds = mutableListOf<String>()

    private val now = ZonedDateTime.now()
    private val yesterday = ZonedDateTime.now().minusDays(1)
    private val twoDaysAgo = ZonedDateTime.now().minusDays(2)
    private val agesAgo = ZonedDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneId.of("Europe/Oslo"))

    @MockkBean(PersonOppslag::class)
    private lateinit var personOppslag: PersonOppslag

    @MockkBean(VarselPublisher::class)
    private lateinit var varselPublisher: VarselPublisher

    @BeforeEach
    fun setupAktoerId() {
        aktoer = rnd.nextDouble().toString().take(11)
        aktoerFnr = rnd.nextDouble().toString().take(11)

        if(usedIds.contains(aktoer) || usedIds.contains(aktoerFnr))
            throw Exception("Used id")

        usedIds.add(aktoer)
        usedIds.add(aktoerFnr)

        every { personOppslag.hentAktorId(aktoerFnr) } returns aktoer
        every { personOppslag.hentAktorId(aktoer) } returns aktoerFnr

        every { varselPublisher.publish(any(), aktoerFnr) } returns Unit
        every { varselPublisher.done(any(), aktoerFnr) } returns Unit
    }


    @Autowired
    lateinit var statusRepository: StatusRepository

    @Autowired
    lateinit var hendelseService: HendelseService


    @Test
    fun `ny bruker - riktig aktoerId`() {
        val nyBruker = Status.nySession(aktoer)

        assertEquals(aktoer, nyBruker.aktoerId)
    }

    @Test
    fun `ny bruker - har sett cv - status cvOppdatert`() {
        hendelseService.harSettCv(aktoer, now)

        val lagretStatus = statusRepository.finnSiste(aktoer)

        assertEquals(cvOppdatertStatus, lagretStatus.status)
        assertEquals(now, lagretStatus.statusTidspunkt)
    }

    @Test
    fun `ny bruker - har kommet under oppfoelging - status forGammel`() {
        hendelseService.harKommetUnderOppfolging(aktoer, agesAgo)

        val lagretStatus = statusRepository.finnSiste(aktoer)

        assertEquals(forGammelStatus, lagretStatus.status)
        assertEquals(agesAgo, lagretStatus.statusTidspunkt)
    }

    @Test
    fun `for gammel – har sett cv – status cvOppdatert`() {
        val status = Status.nySession(aktoer).forGammel(agesAgo)
        statusRepository.lagre(status)
        hendelseService.harSettCv(aktoer, yesterday)

        val lagretStatus = statusRepository.finnSiste(aktoer)

        verify(exactly = 0) { varselPublisher.done(any(), any()) }
        assertEquals(cvOppdatertStatus, lagretStatus.status)
        assertEquals(yesterday, lagretStatus.statusTidspunkt)
    }

    @Test
    fun `for gammel – har kommet under oppfoelging – status skalVarslesManglerFoedselsnummer`() {
        val status = Status.nySession(aktoer).forGammel(agesAgo)
        statusRepository.lagre(status)
        hendelseService.harKommetUnderOppfolging(aktoer, yesterday)

        val lagretStatus = statusRepository.finnSiste(aktoer)

        assertEquals(skalVarslesManglerFnrStatus, lagretStatus.status)
        assertEquals(yesterday, lagretStatus.statusTidspunkt)
    }

    @Test
    fun `ny bruker - har kommet under oppfoelging - status skalVarslesManglerFoedselsnummer`() {
        hendelseService.harKommetUnderOppfolging(aktoer, yesterday)

        val lagretStatus = statusRepository.finnSiste(aktoer)

        assertEquals(skalVarslesManglerFnrStatus, lagretStatus.status)
        assertEquals(yesterday, lagretStatus.statusTidspunkt)
    }

    @Test
    fun `bruker under oppfoelging - fnr funnet - status skalVarsles`() {
        val status = Status.skalVarlsesManglerFnr(Status.nySession("dummy2"), now)
        statusRepository.lagre(status)
        hendelseService.funnetFodselsnummer("dummy2", "dummy_fnr")

        val lagretStatus = statusRepository.finnSiste("dummy2")

        assertEquals(skalVarslesStatus, lagretStatus.status)
    }

    @Test
    fun `bruker under oppfoelging – varsle bruker – status varslet`() {
        val status = Status.nySession(aktoer).skalVarsles(aktoerFnr)
        statusRepository.lagre(status)
        hendelseService.varsleBruker(aktoer)

        val lagretStatus = statusRepository.finnSiste(aktoer)

        verify { varselPublisher.publish(any(), aktoerFnr) }
        assertEquals (varsletStatus, lagretStatus.status)
    }

    @Test
    fun `bruker varslet – har sett cv – status cvOppdatert`() {
        val status = Status.nySession(aktoer)
            .skalVarlsesManglerFnr(twoDaysAgo)
            .skalVarsles(aktoerFnr)
            .varslet(yesterday)

        statusRepository.lagre(status)
        hendelseService.harSettCv(aktoer, now)

        val lagretStatus = statusRepository.finnSiste(aktoer)

        verify { varselPublisher.done(any(), aktoerFnr) }
        assertEquals (cvOppdatertStatus, lagretStatus.status)
    }

    @Test
    fun `bruker varslet – melding fra pto – status ikkeUnderOppfoelging`() {
        val status = Status.nySession(aktoer)
            .skalVarlsesManglerFnr(twoDaysAgo)
            .skalVarsles(aktoerFnr)
            .varslet(yesterday)

        statusRepository.lagre(status)
        hendelseService.ikkeUnderOppfolging(aktoer, now)

        val lagretStatus = statusRepository.finnSiste(aktoer)

        verify { varselPublisher.done(any(), aktoerFnr) }
        assertEquals (ikkeUnderOppfølgingStatus, lagretStatus.status)
    }

    @Test
    fun `ikke under oppfoelging – har kommet under oppfoelging – status skalVarslesManglerFoedselsnummer`() {
        val status = Status.ikkeUnderOppfolging(statusRepository.finnSiste(aktoer), twoDaysAgo)
        val prevUUID = status.uuid
        statusRepository.lagre(status)
        hendelseService.harKommetUnderOppfolging(aktoer, yesterday)

        val lagretStatus = statusRepository.finnSiste(aktoer)

        assertNotEquals(prevUUID, lagretStatus.uuid)
        assertEquals(skalVarslesManglerFnrStatus, lagretStatus.status)
        assertEquals(yesterday, lagretStatus.statusTidspunkt)
    }

    @Test
    fun `ikke under oppfoelging – har sett cv – status cvOppdatert`() {
        val status = Status.ikkeUnderOppfolging(forrigeStatus = statusRepository.finnSiste(aktoer), tidspunkt = twoDaysAgo)
        statusRepository.lagre(status)
        hendelseService.harSettCv(aktoer, yesterday)

        val lagretStatus = statusRepository.finnSiste(aktoer)

        assertEquals(cvOppdatertStatus, lagretStatus.status)
        assertEquals(yesterday, lagretStatus.statusTidspunkt)
    }

}