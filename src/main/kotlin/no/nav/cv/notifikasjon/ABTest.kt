package no.nav.cv.notifikasjon

import org.springframework.stereotype.Service

interface ABTestSelector {
    fun skalVarsles() : Boolean
}

@Service
class ABTestIncludeAll : ABTestSelector {
    override fun skalVarsles() = true
}
