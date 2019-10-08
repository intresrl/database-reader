package it.intre.code.database.reader.filter.generic

import java.util.Collections.emptyList

abstract class ParseHandler internal constructor(private val params: Map<String, List<String>>) {

    fun toFilter(name: String): GenericFilter {
        val filter = this.toSpecificFilter(name)
        filter.all = filter.all || toBoolean(name, ALL_SUFFIX)
        filter.negate = toBoolean(name, NEGATE_SUFFIX)
        return filter
    }

    internal abstract fun toSpecificFilter(name: String): GenericFilter

    internal fun getValues(name: String) = params[name] ?: emptyList()

    internal fun toBoolean(name: String, suffix: String): Boolean {
        val list = params[name + suffix] ?: return false

        /* true only if it has at least one valid value and all values are true */
        return toBoolean(list)
    }

    companion object {

        /**
         * Keys starting with [GenericFilterParser.PREFIX] and ending with this suffix
         * will set the option [TextFilter.negate] on the filter
         * of the corresponding field: the name is obtained by stripping away prefix and suffix.
         *
         *
         * Example: ` f.foo=["1"] ; f.foo.negate=["true"] `
         * leads to a filter with name = "foo", values = ["1"] and negate = true
         */
        const val NEGATE_SUFFIX = ".negate"
        /**
         * Keys starting with [GenericFilterParser.PREFIX] and ending with this suffix
         * will set the option [TextFilter.all] on the filter
         * of the corresponding field: the name is obtained by stripping away prefix and suffix.
         *
         *
         * Example: ` f.foo=["1"] ; f.foo.all=["true"] `
         * leads to a filter with name = "foo", values = ["1"] and all = true
         */
        const val ALL_SUFFIX = ".all"

        @JvmStatic
        fun toBoolean(list: List<String?>) = list
                .map { it?.toBoolean() ?: false }
                .minBy { it } == true
    }
}

