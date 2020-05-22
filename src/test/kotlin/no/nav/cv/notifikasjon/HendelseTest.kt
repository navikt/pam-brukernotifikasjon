//package no.nav.cv.notifikasjon
//
//import org.junit.jupiter.api.Test
//import assertk.assertThat
//import assertk.assertions.*
//import io.mockk.mockk
//import java.time.ZonedDateTime
//import java.util.*
//
//class HendelseTest {
//
//    private val fnr = "dummy"
//
//    val varselPublisher = mockk<VarselPublisher>(relaxed = true)
//
//    private val now = ZonedDateTime.now()
//    private val yesterday = ZonedDateTime.now().minusDays(1)
//
//    @Test
//    fun `status ukjent - hendelse kommetUnderOppfolging - forer til varsel`() {
//        val opprinneligStatus = Status.ukjent(fnr)
//        val nyStatus = Hendelse.kommetUnderOppfolging(fnr, opprinneligStatus, now).produserVarsel().varsle(varselPublisher)
//        assertThat(nyStatus.status).isEqualTo(varsletStatus)
//    }
//
//    @Test
//    fun `status ukjent - hendelse erFulgtOpp - forer ikke til varsel`() {
//        val opprinneligStatus = Status.ukjent(fnr)
//        val nyStatus = Hendelse.erFulgtOpp(fnr, opprinneligStatus, now).produserVarsel().varsle(varselPublisher)
//        assertThat(nyStatus.status).isEqualTo(ignorertHendelseStatus)
//    }
//
//    @Test
//    fun `status ukjent - hendelse settCv - forer ikke til varsel`() {
//        val opprinneligStatus = Status.ukjent(fnr)
//        val nyStatus = Hendelse.settCv(fnr, opprinneligStatus, now).produserVarsel().varsle(varselPublisher)
//        assertThat(nyStatus.status).isEqualTo(ignorertHendelseStatus)
//    }
//
//
//
//    @Test
//    fun `status done - hendelse kommetUnderOppfolging - forer til varsel`() {
//        val opprinneligStatus = Status.done(UUID.randomUUID(), fnr, yesterday)
//        val nyStatus = Hendelse.kommetUnderOppfolging(fnr, opprinneligStatus, now).produserVarsel().varsle(varselPublisher)
//        assertThat(nyStatus.status).isEqualTo(varsletStatus)
//    }
//
//    @Test
//    fun `status done - hendelse erFulgtOpp - forer ikke til varsel`() {
//        val opprinneligStatus = Status.done(UUID.randomUUID(), fnr, yesterday)
//        val nyStatus = Hendelse.erFulgtOpp(fnr, opprinneligStatus, now).produserVarsel().varsle(varselPublisher)
//        assertThat(nyStatus.status).isEqualTo(ignorertHendelseStatus)
//    }
//
//    @Test
//    fun `status done - hendelse settCv - forer ikke til varsel`() {
//        val opprinneligStatus = Status.done(UUID.randomUUID(), fnr, yesterday)
//        val nyStatus = Hendelse.settCv(fnr, opprinneligStatus, now).produserVarsel().varsle(varselPublisher)
//        assertThat(nyStatus.status).isEqualTo(ignorertHendelseStatus)
//    }
//
//
//    @Test
//    fun `status varslet - hendelse kommetUnderOppfolging - forer ikke til varsel`() {
//        val opprinneligStatus = Status.varslet(UUID.randomUUID(), fnr, yesterday)
//        val nyStatus = Hendelse.kommetUnderOppfolging(fnr, opprinneligStatus, now).produserVarsel().varsle(varselPublisher)
//        assertThat(nyStatus.status).isEqualTo(ignorertHendelseStatus)
//    }
//
//    @Test
//    fun `status varslet - hendelse erFulgtOpp - forer ikke til done-melding`() {
//        val opprinneligStatus = Status.varslet(UUID.randomUUID(), fnr, yesterday)
//        val nyStatus = Hendelse.erFulgtOpp(fnr, opprinneligStatus, now).produserVarsel().varsle(varselPublisher)
//        assertThat(nyStatus.status).isEqualTo(doneStatus)
//    }
//
//    @Test
//    fun `status varslet - hendelse settCv - forer ikke til done-melding`() {
//        val opprinneligStatus = Status.done(UUID.randomUUID(), fnr, yesterday)
//        val nyStatus = Hendelse.settCv(fnr, opprinneligStatus, now).produserVarsel().varsle(varselPublisher)
//        assertThat(nyStatus.status).isEqualTo(ignorertHendelseStatus)
//    }
//
//}