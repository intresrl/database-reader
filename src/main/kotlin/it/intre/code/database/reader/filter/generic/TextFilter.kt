package it.intre.code.database.reader.filter.generic

data class TextFilter(
        override var name: String,
        override var all: Boolean,
        override var negate: Boolean,
        /** List of possible values */
        var values: List<String>,
        /** If true, it means that null value is valid in addition to [.getValues]. */
        var withBlank: Boolean
) : GenericFilter(name, all, negate) {

    private val isFilteringByValues: Boolean
        get() = !all && (values.isNotEmpty() || withBlank)

    override fun hasCondition(): Boolean {
        return negate || isFilteringByValues
    }
}
