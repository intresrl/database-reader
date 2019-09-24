package it.intre.code.database.reader.sql

import it.intre.code.database.reader.filter.generic.ComparisonFilter
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.StringUtils.isBlank
import org.apache.commons.lang3.StringUtils.wrap

class ComparisonFilterBuilder internal constructor(override val filter: ComparisonFilter, name: String) : SqlFilterBuilder(name) {
    private val asString: Boolean

    override val orConditions: List<String>
        get() {
            val condition = listOf(
                    concat(">=", filter.ge),
                    concat(">", filter.gt),
                    concat("<=", filter.le),
                    concat("<", filter.lt)
            )
                    .filter { StringUtils.isNotBlank(it) }
                    .joinToString(separator = " AND ")
            val response = if (isBlank(condition)) SqlHelper.TRUE else condition
            return listOf(response)
        }

    init {
        this.asString = isForString(filter)
    }

    private fun isForString(filter: ComparisonFilter): Boolean {
        val datatype = filter.datatype
        return isBlank(datatype) || ComparisonFilter.DATATYPE_STRING.equals(datatype, ignoreCase = true)
    }

    private fun concat(operator: String, value: Any?): String {
        return if (value == null) "" else this.name + operator + format(value)
    }

    private fun format(o: Any): String {
        val s = o.toString()
        return if (asString) wrap(s, "'") else s
    }

}
