package it.intre.code.database.reader.dao

import it.intre.code.database.reader.filter.FilterContainer
import it.intre.code.database.reader.resultset.ReaderResultSet


interface ReaderDao {

    fun find(filter: FilterContainer): ReaderResultSet

    fun getColumns(filter: FilterContainer): ReaderResultSet

}
