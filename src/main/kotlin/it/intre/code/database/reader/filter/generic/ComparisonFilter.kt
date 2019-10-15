package it.intre.code.database.reader.filter.generic

data class ComparisonFilter(
        override var name: String,
        override var all: Boolean = false,
        override var negate: Boolean = false,
        /** Greater than */
        var gt: Any? = null,
        /** Greater than or equal to */
        var ge: Any? = null,
        /** Less than */
        var lt: Any? = null,
        /** Less than or equal to */
        var le: Any? = null,
        /** [.DATATYPE_STRING] (default if null) or #DATATYPE_INTEGER */
        var datatype: String? = null
) : GenericFilter(name, all, negate) {

    override fun hasCondition() = !hasNoCondition()

    fun hasNoCondition() = all && !negate || ge == null && gt == null && le == null && lt == null

    companion object {
        const val DATATYPE_STRING = "String"
        const val DATATYPE_INTEGER = "Integer"
    }


}
