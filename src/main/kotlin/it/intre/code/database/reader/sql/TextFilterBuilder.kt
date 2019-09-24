package it.intre.code.database.reader.sql

import it.intre.code.database.reader.filter.generic.FilterConstants.WILDCARD_ALL_DST
import it.intre.code.database.reader.filter.generic.FilterConstants.WILDCARD_ALL_SRC
import it.intre.code.database.reader.filter.generic.TextFilter
import org.apache.commons.lang3.StringUtils.defaultString
import java.lang.String.format

/**
 * SQL builder for filters of type text, matching on "=" or "LIKE"
 */
internal class TextFilterBuilder(override val filter: TextFilter, name: String) : SqlFilterBuilder(name) {

    override val orConditions: List<String>
        get() = blankStream() + valuesStream()

    private fun blankStream(): List<String> = if (filter.withBlank) listOf(this.name + IS_NULL) else listOf()

    private fun valuesStream(): List<String> = filter.values.map { this.toCondition(it) }

    private fun toCondition(value: String): String {
        //FIXME DAMIANO SANITIZE!!
        val v = defaultString(value).toUpperCase().replace(WILDCARD_ALL_SRC, WILDCARD_ALL_DST)
        val hasWildcard = v.indexOf(WILDCARD_ALL_DST) >= 0
        return format("UPPER(%s)", this.name) + if (hasWildcard) toLikeCondition(v) else toEqualCondition(v)
    }

    private fun toLikeCondition(value: String): String {
        return format(" LIKE '%s' ESCAPE '%s'", escape(value), ESCAPE_CHAR)
    }

    private fun escape(value: String): String {
        return value.replace("_", ESCAPE_CHAR + "_")
    }

    private fun toEqualCondition(value: String): String {
        return format("='%s'", value)
    }

    /**
     * Overridden to manage negation.
     * E.g. for a field with values 'a', 'b' and NULL, the condition with
     * [TextFilter.values] = ['a'], [TextFilter.withBlank] = false,
     * [TextFilter.negate] = true can't be just "NOT(field='a')"
     * because the would exclude NULLs as well, in contrast with isWithBlank=false.
     * Instead, condition must be "(NOT(field='a') OR field IS NULL)"
     */
    override fun toSql(): String {
        val sql = super.toSql()
        return if (filter.negate && !filter.withBlank && !filter.all) {
            "($sql$OR${this.name}$IS_NULL)"
        } else sql
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TextFilterBuilder

        if (filter != other.filter) return false

        return true
    }

    override fun hashCode(): Int {
        return filter.hashCode()
    }

    companion object {

        private const val IS_NULL = " IS NULL"

        private const val ESCAPE_CHAR = '\\'
    }

}
