package it.intre.code.database.reader.filter

import it.intre.code.database.reader.filter.generic.GenericFilter
import javax.ws.rs.core.MultivaluedMap

interface FilterContainer {
    val paramValues: MultivaluedMap<String, String>

    val paramNames: Set<String>

    val isProfileSet: Boolean

    val isPaginationSqlSet: Boolean

    val page: Int?

    val pageFrom: Int?

    val pageTo: Int?

    val size: Int?

    var profile: String

    var customFilters: List<GenericFilter>

    var queryStringFilter: QueryStringFilter

    fun parseFrom(params: MultivaluedMap<String, String>)

    fun getParamValues(key: String): List<String>
}
