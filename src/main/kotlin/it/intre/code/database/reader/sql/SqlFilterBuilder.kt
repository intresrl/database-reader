package it.intre.code.database.reader.sql

import it.intre.code.database.reader.config.QueryProfile
import it.intre.code.database.reader.filter.generic.ComparisonFilter
import it.intre.code.database.reader.filter.generic.GenericFilter
import it.intre.code.database.reader.filter.generic.TextFilter
import org.apache.commons.lang3.StringUtils
import java.lang.String.format
import java.util.Collections.emptyList

/**
 * Abstract SQL builder for filters.
 * To manage a new filter type: extend this class,
 * implement abstract methods and add new implementation to factory method.
 */
//FIXME implement equals and hashCode using fields: name
abstract class SqlFilterBuilder(val name: String) {

    internal abstract val filter: GenericFilter

    internal abstract val orConditions: List<String>

    open fun toSql(): String {
        return format("%s(%s)", if (filter.negate) NOT else "", conditions())
    }

    private fun conditions(): String {
        val conditions = if (filter.all)
            emptyList<Any>()
        else
            orConditions
        return if (conditions.isEmpty()) SqlHelper.TRUE else StringUtils.join(conditions, OR)
    }

    companion object {

        private const val NOT = " NOT"
        const val OR = " OR "

        fun from(filter: GenericFilter, queryProfile: QueryProfile?): SqlFilterBuilder {
            val name = getFieldNameOrExpression(filter, queryProfile)
            return when (filter) {
                is TextFilter -> TextFilterBuilder(filter, name)
                is ComparisonFilter -> ComparisonFilterBuilder(filter, name)
                else -> throw IllegalArgumentException()
            }
        }

        private fun getFieldNameOrExpression(filter: GenericFilter, queryProfile: QueryProfile?): String {
            return toFieldNameOrExpression(filter.name, queryProfile)
        }

        fun toFieldNameOrExpression(columnAlias: String, queryProfile: QueryProfile?): String {

            val columns = queryProfile?.columns
            if (columns != null) {
                for (column in columns) {
                    if (column.isDerived && columnAlias.equals(column.alias, ignoreCase = true)) {
                        return column.name
                    }
                }
            }
            return columnAlias
        }
    }
}
