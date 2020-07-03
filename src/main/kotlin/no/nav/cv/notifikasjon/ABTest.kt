package no.nav.cv.notifikasjon

import java.lang.Math.random
import javax.inject.Named
import javax.inject.Qualifier
import javax.inject.Singleton

interface ABTestSelector {
    fun skalVarsles() : Boolean
}

@Singleton
class ABTestIncludeHalf : ABTestSelector {
    override fun skalVarsles() = random() < 0.5
}