package it.intre.code.database.reader.sql

import it.intre.code.database.reader.config.Column
import it.intre.code.database.reader.config.Column.NameGenerator.*
import it.intre.code.database.reader.config.QueryProfile
import it.intre.code.database.reader.filter.FilterContainer
import it.intre.code.database.reader.filter.generic.GenericFilter
import it.intre.code.database.reader.sql.SqlFilterBuilder.Companion.toFieldNameOrExpression
import org.apache.commons.lang3.StringUtils.defaultString
import org.slf4j.LoggerFactory
import java.lang.String.format
import java.util.*

/**
 * Builder of SQL queries starting from [FilterContainer] and [QueryProfile]
 */
internal class SqlHelper private constructor() {

    init {
        throw UnsupportedOperationException("This is a utility class and cannot be instantiated")
    }

    internal class Range(val from: Int, val to: Int) {

        override fun equals(other: Any?): Boolean {
            if (other === this) return true
            if (other !is Range) return false
            val range = other as Range?
            if (!range!!.canEqual(this as Any)) return false
            if (this.from != range.from) return false
            return if (this.to != range.to) false else true
        }

        override fun hashCode(): Int {
            val PRIME = 59
            var result = 1
            result = result * PRIME + this.from
            result = result * PRIME + this.to
            return result
        }

        override fun toString(): String {
            return "SqlHelper.Range(from=" + this.from + ", to=" + this.to + ")"
        }

        protected fun canEqual(other: Any): Boolean {
            return other is Range
        }
    }

    companion object {

        const val TRUE = "1=1"
        private val logger = LoggerFactory.getLogger(SqlHelper::class.java)

        fun paginationInnerQuery(filter: FilterContainer, queryProfile: QueryProfile, clauses: String): String {
            val fieldNames = fields(filter, queryProfile, NAME_WITH_ALIAS)
            if (filter.isPaginationSqlSet && queryProfile.isPaginationEnabled) {
                val hasDerived = hasDerivedFields(queryProfile)
                val page = filter.page
                val pageFrom = filter.pageFrom
                val pageTo = filter.pageTo
                val size = filter.size

                val range = computeRowRange(page, pageFrom, pageTo, size)

                if (hasDerived) {
                    //FIXME DAMIANO always use this version (tests must be adpated, but there are no other side effects)
                    val names = fields(filter, queryProfile, ONLY_NAME_NO_DERIVED)
                    return (" SELECT "
                            + fieldNames
                            + " FROM ( SELECT " + names
                            + ", rownum AS rnum"
                            + " FROM ( "
                            + "SELECT " + names + " "
                            + defaultString(clauses)
                            + ")"
                            + " WHERE rownum <= "
                            + range.to
                            + " ) "
                            + " WHERE  rnum > "
                            + range.from)
                } else {
                    val aliases = fields(filter, queryProfile, ONLY_ALIAS)
                    return ("SELECT " + aliases
                            + " FROM ( SELECT " + aliases
                            + ", rownum AS rnum"
                            + " FROM ( "
                            + " SELECT "
                            + fieldNames + " "
                            + defaultString(clauses)
                            + ")"
                            + " WHERE rownum <= "
                            + range.to
                            + " ) "
                            + " WHERE  rnum > "
                            + range.from)
                }
            }
            return " SELECT " + fieldNames + " " + defaultString(clauses)
        }

        fun computeRowRange(page: Int?,
                            pageFrom: Int?,
                            pageTo: Int?,
                            size: Int?): Range {
            val from: Int
            val to: Int
            if (page != null) {
                from = page * size!!
                to = from + size
            } else {
                from = pageFrom!! * size!!
                to = (pageTo!! + 1) * size
            }
            if (from > to) {
                logger.error(format("FIRST ROW > LAST ROW! page = %d / pageFrom = %d / pageTo = %d / size = %d ===> [from, to] = [%d, %d]",
                        page, pageFrom, pageTo, size, from, to))
            }
            return Range(from, to)
        }

        fun orderBy(filter: FilterContainer, queryProfile: QueryProfile): String {
            val orderFields = filter.queryStringFilter.orderFields
            if (orderFields.isEmpty()) {
                return ""
            }
            val orderByClause = orderFields.joinToString(separator = ", ") { (fieldId, orderDirection) ->
                format("%s %s",
                        toFieldNameOrExpression(fieldId, queryProfile),
                        orderDirection
                )
            }
            return " order by $orderByClause"
        }

        fun andOrFilter(filters: List<GenericFilter>, queryProfile: QueryProfile): String {
            return filters
                    .map { filter -> SqlFilterBuilder.from(filter, queryProfile) }
                    .map { it.toSql() }
                    .joinToString("") { " and $it" }
        }

        fun groupBy(filter: FilterContainer): String {
            val qsf = filter.queryStringFilter
            return when {
                qsf.hasGroupBy() -> " group by " + qsf.getGroupAsString("")
                else -> ""
            }
        }

        fun startWhere(): String {
            return " where $TRUE "
        }

        private fun hasDerivedFields(queryProfile: QueryProfile?): Boolean {
            if (queryProfile != null) {
                val columns = queryProfile.columns ?: emptyList()
                for (col in columns) {
                    if (col.isDerived) {
                        return true
                    }
                }
            }
            return false
        }

        // TODO: suppress fields if not present in filter, * return all in profile
        fun fields(filter: FilterContainer, queryProfile: QueryProfile?, nameGenerator: Column.NameGenerator): String {
            if (queryProfile != null) {
                val fields = ArrayList<String>()
                val columns = queryProfile.columns ?: emptyList()
                for (col in columns) {
                    if (!_isFieldToRemoveFromSelect(filter, col)) {
                        val field = nameGenerator.generate(col)
                        if (!field.isEmpty()) {
                            fields.add(field)
                        }
                    }
                }
                if (!fields.isEmpty()) {
                    return fields.joinToString(" , ")
                }
            }

            return " * "
        }

        private fun _isFieldToRemoveFromSelect(filter: FilterContainer, col: Column): Boolean {
            val qsf = filter.queryStringFilter
            val reportFields = qsf.reportFields
            return when {
                reportFields.isNotEmpty() -> !reportFields.contains(col.name.toLowerCase())
                else -> !col.isAggregate && qsf.hasGroupBy() && !qsf.isContainedInGroup(col)
            }
        }

        /**
         * Remove all whitespaces between non-words; replace all whitespaces between words with a single space.
         */
        fun normalizeSpaces(sql: String): String {
            return sql
                    .replace("\\s+".toRegex(), " ")
                    .replace("\\B | \\B".toRegex(), "")
        }
    }
}
