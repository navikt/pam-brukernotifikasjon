package no.nav.cv.notifikasjon

class ABTest(private val skalVarsles: Boolean) : ABTestSelector {
    companion object {
        val skalVarsles: ABTestSelector = ABTest(true)
        val skalIkkeVarsles: ABTestSelector = ABTest(false)
    }

    override fun skalVarsles() = skalVarsles
}
