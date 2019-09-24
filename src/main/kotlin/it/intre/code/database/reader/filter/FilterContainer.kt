package it.intre.code.database.reader.filter

import it.intre.code.database.reader.filter.generic.GenericFilter
import it.intre.code.database.reader.filter.generic.GenericFilterParser
import org.apache.commons.lang3.StringUtils.isNotEmpty
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
        get() = isNotEmpty(profile)

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

}