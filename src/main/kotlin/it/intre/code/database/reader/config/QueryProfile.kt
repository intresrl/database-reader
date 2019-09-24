package it.intre.code.database.reader.config

import it.intre.code.database.reader.filter.FilterContainer
import org.apache.commons.lang3.StringUtils.isNotEmpty
import java.util.*

/**
 * Model for the profile of each query inside [ReadProfile].
 */
class QueryProfile {

    var table: String? = ""

    /**
     * Name of an external query profile (name of the file without extension) from which to copy all values other than table.
     */
    var include: String? = null

    var columns: List<Column>? = null
    var where: String? = ""
    var group: String? = ""
    var having: String? = ""
    var order: String? = ""
    var maxRecords: Int? = null
    var isDisablePagination = false

    /**
     * Optional map from field names to special WHERE conditions for those fields.
     *
     *
     * Each value may contain the placeholder [ExtraWhereResolver.PLACEHOLDER] that will be replaced by provided field value.
     *
     *
     * E.g. for the map {"foo":"(foo is null or foo=?)", "bar": "nvl(bar,baz)=2"},
     * if query parameters contain foo=10 and bar=20, the WHERE conditions
     * "(foo is null or foo=10) AND nvl(bar,baz)=2" will be created.
     *
     *
     * Keys that are not in the query parameters will be ignored.
     * E.g. with the same map, if query parameters are only foo=30,
     * the WHERE condition will be "(foo is null or foo=30)"
     */
    var extraWhere: Map<String, String>? = HashMap()

    val isPaginationEnabled: Boolean
        get() = !this.isDisablePagination

    val isWhere: Boolean
        get() = isNotEmpty(where)

    val isGroup: Boolean
        get() = isNotEmpty(group)

    val isHaving: Boolean
        get() = isNotEmpty(having)

    val isOrder: Boolean
        get() = isNotEmpty(order)

    val isMaxRecords: Boolean
        get() = maxRecords != null && maxRecords!! > 0

    /**
     * In each [.getExtraWhere] clause, replace placeholders with filter values.
     * Spe
     * If the clause contains markers [ExtraWhereResolver.REPEAT_START]/[ExtraWhereResolver.REPEAT_END], the string
     * contained inside will be repeated for each value; otherwise, only first value is used.
     *
     *
     * E.g. with values for a field ["A","B","C"]:
     * - "foo > ?" becomes "foo > A"
     * - "bar < 0###REPEAT{+?}###" becomes "bar < 0+A+B+C"
     */
    fun resolveMacro(filter: FilterContainer) {
        val newExtraWhere = HashMap<String, String>()
        for (key in filter.paramNames) {
            val values = filter.getParamValues(key) ?: emptyList()
            val customCondition = extraWhere!![key] ?: ""
            if (isNotEmpty(customCondition)) {
                newExtraWhere[key] = ExtraWhereResolver.resolve(customCondition, values)
            }
        }
        extraWhere = newExtraWhere
    }

    /**
     * Copy all field values other than [.getTable] and [.getWhere] from given profile.
     */
    internal fun copyFrom(other: QueryProfile) {
        columns = other.columns
        isDisablePagination = other.isDisablePagination
        extraWhere = other.extraWhere
        group = other.group
        having = other.having
        maxRecords = other.maxRecords
        order = other.order
        where = other.where
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as QueryProfile

        if (table != other.table) return false
        if (include != other.include) return false
        if (columns != other.columns) return false
        if (where != other.where) return false
        if (group != other.group) return false
        if (having != other.having) return false
        if (order != other.order) return false
        if (maxRecords != other.maxRecords) return false
        if (isDisablePagination != other.isDisablePagination) return false
        if (extraWhere != other.extraWhere) return false

        return true
    }

    override fun hashCode(): Int {
        var result = table?.hashCode() ?: 0
        result = 31 * result + (include?.hashCode() ?: 0)
        result = 31 * result + (columns?.hashCode() ?: 0)
        result = 31 * result + (where?.hashCode() ?: 0)
        result = 31 * result + (group?.hashCode() ?: 0)
        result = 31 * result + (having?.hashCode() ?: 0)
        result = 31 * result + (order?.hashCode() ?: 0)
        result = 31 * result + (maxRecords ?: 0)
        result = 31 * result + isDisablePagination.hashCode()
        result = 31 * result + (extraWhere?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "QueryProfile(table=$table, include=$include, columns=$columns, where=$where, group=$group, having=$having, order=$order, maxRecords=$maxRecords, isDisablePagination=$isDisablePagination, extraWhere=$extraWhere)"
    }


}
