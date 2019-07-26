package it.intre.code.database.reader.filter

import it.intre.code.database.reader.config.Column

interface QueryStringFilter {

    val orderFields: List<OrderField>

    var last: String

    var page: Int?

    var pageFrom: Int?

    var pageTo: Int?

    var size: Int?

    var reportFields: List<String>

    var groupBy: List<String>
    fun getGroupAsString(toRemove: String): String

    fun isContainedInGroup(column: Column): Boolean

    fun hasGroupBy(): Boolean

    fun setOrderBy(orderBy: List<String>)

    fun setOrderDir(orderDir: List<String>)


}
