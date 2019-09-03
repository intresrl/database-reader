package it.intre.code.database.reader.filter.generic

data class ComparisonFilter(
        override var name: String,
        override var all: Boolean,
        override var negate: Boolean,
        /** Greater than */
        var gt: Any?,
        /** Greater than or equal to */
        var ge: Any?,
        /** Less than */
        var lt: Any?,
        /** Less than or equal to */
        var le: Any?,
        /** [.DATATYPE_STRING] (default if null) or #DATATYPE_INTEGER */
        var datatype: String?
) : GenericFilter(name, all, negate) {

    override fun hasCondition(): Boolean {
        return ge != null || gt != null || le != null || lt != null
    }

    companion object {
        val DATATYPE_STRING = "String"
        val DATATYPE_INTEGER = "Integer"
    }
}
