package it.intre.code.database.reader.service

import it.intre.code.database.reader.dao.GenericDataSource
import it.intre.code.database.reader.dao.ReaderDao
import it.intre.code.database.reader.filter.FilterContainer
import it.intre.code.database.reader.resultset.ReaderResultSet

class ReaderService private constructor(private val readerDao: ReaderDao) {

    companion object {
        fun make(readerDao: ReaderDao) = ReaderService(readerDao)
    }

    fun setGenericDataSource(genericDataSource: GenericDataSource) {
        readerDao.setGenericDataSource(genericDataSource)
    }

    fun find(filter: FilterContainer): ReaderResultSet {
        return execQuery(filter) { readerDao.find(it) }
    }

    fun getColumns(filter: FilterContainer): ReaderResultSet {
        return execQuery(filter) { readerDao.getColumns(it) }
    }

    private fun execQuery(filter: FilterContainer, query: (FilterContainer) -> ReaderResultSet): ReaderResultSet {
        var result = ReaderResultSet()
        try {
            result = query(filter)
        } catch (e: Exception) {
            result.isError = true
            result.errorMessage = e.localizedMessage
        }

        return result
    }

}
