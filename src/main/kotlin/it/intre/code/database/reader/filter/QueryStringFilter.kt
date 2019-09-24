package it.intre.code.database.reader.filter

import it.intre.code.database.reader.config.Column
import kotlin.math.max

/**
 * Container for all standard query options, taken from query string (e.g."?page=3&size=50")
 */
data class QueryStringFilter internal constructor(
        /**
         * If not blank, return only the last value of field with this name and the number of rows.
         */
        var last: String? = null,
        /**
         * Page number (starting from 0)
         */
        var page: Int? = null,
        /* The following two parameters (pageFrom and pageTo) are used in alternative to "page" parameter when getting more than one page */
        /**
         * First page number
         */
        var pageFrom: Int? = null,
        /**
         * Last page number
         */
        var pageTo: Int? = null,
        /**
         * Page size
         */
        var size: Int? = null,
        /**
         * Optional list of fields to SELECT (if empty, all fields in profile will be returned)
         */
        var reportFields: List<String> = listOf(),
        /**
         * Optional list of fields to use in GROUP BY clause
         */
        var groupBy: List<String> = listOf(),
        /**
         * List of fields to use in ORDER BY clause.
         * Works in pair with [.orderDir] to produce [.getOrderFields].
         */
        // FIXME DAMIANO parallel lists... replace with map?
        private var orderBy: List<String>? = listOf(),
        /**
         * List of ordering directions in ORDER BY clause.
         * Works in pair with [.orderBy] to produce [.getOrderFields].
         */
        private var orderDir: List<String>? = listOf()) {


    val orderFields: List<OrderField>
        get() {
            val fieldNames = orderBy ?: listOf()
            val fieldDirs = (orderDir ?: listOf())
            val take = generateSequence { OrderField.ASC }.take(max(0,fieldNames.size - fieldDirs.size))
            val dirs = (fieldDirs + take).slice(fieldNames.indices)

            return fieldNames.zip(dirs).map { (fieldId,dir) -> OrderField(fieldId,dir) }
        }

    fun getGroupAsString(toRemove: String?) = groupBy.filter { !it.equals(toRemove, true)}.joinToString(",")


    fun isContainedInGroup(column: Column) = groupBy.any{ x -> x.equals(column.name, true)}

    fun hasGroupBy() = groupBy.isNotEmpty()
}