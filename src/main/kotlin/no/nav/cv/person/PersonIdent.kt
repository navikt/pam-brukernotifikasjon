package no.nav.cv.person

data class PersonIdent(
        private val id: String,
        private val type: Type,
        private val gjeldende: Boolean
) {

    enum class Type {
        FOLKEREGISTER, AKTORID, ANNET
    }

    fun id() = id
    fun type() = type
    fun gjeldende() = gjeldende
}

data class PersonIdenter(private val identer: List<PersonIdent>) {

    companion object {
        val ukjentPerson = PersonIdenter(emptyList())
    }

    fun identerVerdier() = identer.map { it.id() }

    fun identer() = identer

}