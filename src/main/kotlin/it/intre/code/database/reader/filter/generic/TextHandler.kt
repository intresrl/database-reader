package it.intre.code.database.reader.filter.generic

import javax.ws.rs.core.MultivaluedMap


/**
 * Parser for [TextFilter]
 */
class TextHandler internal constructor(params: MultivaluedMap<String, String>) : ParseHandler(params) {

    override fun toSpecificFilter(name: String): TextFilter {
        val values = getValues(name)
        val all = hasAllWildcardFilter(values)
        return TextFilter(GenericFilterParser.noPrefix(name), all, false, values, toBoolean(name, BLANK_SUFFIX))
    }

    private fun hasAllWildcardFilter(values: List<String>): Boolean {
        return values.contains(WILDCARD1) || values.contains(WILDCARD2)
    }

    companion object {

        /**
         * Keys starting with [GenericFilterParser.PREFIX] and ending with this suffix
         * will set the option [TextFilter.withBlank] on the filter
         * of the corresponding field: the name is obtained by stripping away prefix and suffix.
         *
         *
         * Example: ` f.foo=["1"] ; f.foo.blank=["true"] `
         * leads to a filter with name = "foo", values = ["1"] and blank = true
         */
        const val BLANK_SUFFIX = ".blank"

        private const val WILDCARD1: String = FilterConstants.WILDCARD_ALL_SRC.toString()
        private const val WILDCARD2 = FilterConstants.WILDCARD_ALL_DST.toString()
    }

}
