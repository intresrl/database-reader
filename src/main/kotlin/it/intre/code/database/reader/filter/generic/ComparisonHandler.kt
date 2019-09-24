package it.intre.code.database.reader.filter.generic

import it.intre.code.database.reader.filter.generic.ComparisonFilter.Companion.DATATYPE_INTEGER
import it.intre.code.database.reader.filter.generic.ComparisonFilter.Companion.DATATYPE_STRING
import it.intre.code.database.reader.filter.generic.GenericFilterParser.Companion.noPrefix
import javax.ws.rs.core.MultivaluedMap

/**
 * Parser for [ComparisonFilter]
 */
class ComparisonHandler internal constructor(params: MultivaluedMap<String, String>) : ParseHandler(params) {

    override fun toSpecificFilter(name: String): ComparisonFilter {
        val datatype = getDatatype(name)
        val ge = getFirst(name, GE_SUFFIX)
        val gt = getFirst(name, GT_SUFFIX)
        val le = getFirst(name, LE_SUFFIX)
        val lt = getFirst(name, LT_SUFFIX)
        return ComparisonFilter(noPrefix(name), false, false, gt, ge, lt, le, datatype)
    }

    private fun getDatatype(name: String): String? {
        return getValues(name + DATATYPE_SUFFIX)
                .firstOrNull { DATATYPE_INTEGER.equals(it, true) || DATATYPE_STRING.equals(it, true) }
    }

    private fun getFirst(name: String, suffix: String): String? {
        return getValues(name + suffix)
                .firstOrNull()
    }

    companion object {

        /**
         * Same as [.GT_SUFFIX] but for "ge" (&gt;=)
         */
        const val GE_SUFFIX = ".ge"

        /**
         * Keys starting with [GenericFilterParser.PREFIX] and ending with this suffix
         * will set a "&gt;" condition for the corresponding field:
         * the name is obtained by stripping away prefix and suffix.
         *
         *
         * Example: ` f.foo.gt=1 `
         * leads to a [ComparisonFilter] with name = "foo", gt = 1
         */
        const val GT_SUFFIX = ".gt"

        /**
         * Same as [.GT_SUFFIX] but for "le" (&lt;=)
         */
        const val LE_SUFFIX = ".le"

        /**
         * Same as [.GT_SUFFIX] but for "lt" (&lt;)
         */
        const val LT_SUFFIX = ".lt"

        /**
         * Specify datatype, can be "String" (default) or "Integer", case-insensitive
         */
        const val DATATYPE_SUFFIX = ".datatype"
    }
}
