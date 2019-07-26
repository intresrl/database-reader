package it.intre.code.database.reader.filter.generic

interface GenericFilter {

    var name: String

    var isAll: Boolean

    var isNegate: Boolean

    fun hasCondition(): Boolean
}
