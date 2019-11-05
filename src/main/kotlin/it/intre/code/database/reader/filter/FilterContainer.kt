package it.intre.code.database.reader.filter

import it.intre.code.database.reader.filter.generic.GenericFilter
import it.intre.code.database.reader.filter.generic.GenericFilterParser
import java.util.*
import javax.ws.rs.core.MultivaluedHashMap
import javax.ws.rs.core.MultivaluedMap

/**
 * Wrapper for all query filters:
 * - [.getProfile]: profile name;
 * - [.getQueryStringFilter]: [QueryStringFilter] for standard GET / POST parameters;
 * - [.getCustomFilters]: [GenericFilter]s for filters on specific fields of a table.
 */
class FilterContainer {

    var profile: String? = null

    var customFilters: List<GenericFilter> = ArrayList()
    var queryStringFilter = QueryStringFilter()

    var paramValues: MultivaluedMap<String, String> = MultivaluedHashMap()
        private set

    val paramNames: Set<String>
        get() = paramValues.keys

    val isProfileSet: Boolean
        get() = !profile.isNullOrBlank()

    val isPaginationSqlSet: Boolean
        get() = isPageRangeSet && isSizeSet

    private val isPageRangeSet: Boolean
        get() = isValidPage(pageTo) && isValidPage(pageFrom) || isValidPage(page)

    private val isSizeSet: Boolean
        get() = queryStringFilter.size != null && queryStringFilter.size!! > 0

    val page: Int?
        get() = queryStringFilter.page

    val pageFrom: Int?
        get() = queryStringFilter.pageFrom

    val pageTo: Int?
        get() = queryStringFilter.pageTo

    val size: Int?
        get() = queryStringFilter.size

    fun getPaginatedSize() =
            when {
                !isPaginationSqlSet -> -1
                isValidPage(pageTo) && isValidPage(pageFrom) -> size!! * (pageTo!! - pageFrom!! + 1)
                else -> size!!
            }

    fun parseFrom(params: MultivaluedMap<String, String>?) {
        if (params == null)
            return

        val filter = this
        filter.paramValues = params

        filter.customFilters = GenericFilterParser(filter.paramValues).parse()
    }

    fun getParamValues(key: String) = paramValues[key]

    private fun isValidPage(page: Int?): Boolean {
        return page != null && page > -1
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FilterContainer) return false

        if (profile != other.profile) return false
        if (customFilters != other.customFilters) return false
        if (queryStringFilter != other.queryStringFilter) return false
        if (paramValues != other.paramValues) return false

        return true
    }

    override fun hashCode(): Int {
        var result = profile?.hashCode() ?: 0
        result = 31 * result + customFilters.hashCode()
        result = 31 * result + queryStringFilter.hashCode()
        result = 31 * result + paramValues.hashCode()
        return result
    }

    override fun toString(): String {
        return "FilterContainer(profile=$profile, customFilters=$customFilters, queryStringFilter=$queryStringFilter, paramValues=$paramValues)"
    }

}
