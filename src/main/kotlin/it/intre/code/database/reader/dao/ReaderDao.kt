package it.intre.code.database.reader.dao

import it.intre.code.database.reader.config.ProfileLoader
import it.intre.code.database.reader.config.ProfileUrlResolver
import it.intre.code.database.reader.filter.FilterContainer
import it.intre.code.database.reader.resultset.ReaderResultSet

/**
 * Interface for a generic Data Access Object
 */
interface ReaderDao {

    fun find(filter: FilterContainer): ReaderResultSet

    fun getColumns(filter: FilterContainer): ReaderResultSet

    fun setGenericDataSource(genericDataSource: GenericDataSource)

    fun setProfileLoader(profileLoader: ProfileLoader)

    fun setProfileUrlResolver(profileUrlResolver: ProfileUrlResolver)
}
