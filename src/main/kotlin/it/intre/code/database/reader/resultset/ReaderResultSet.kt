package it.intre.code.database.reader.resultset

import it.intre.code.database.reader.config.Column
import it.intre.code.database.reader.filter.FilterContainer

interface ReaderResultSet {

    val durationNanoSeconds: Long

    val durationMilliSeconds: Long

    var list: List<Map<String, Any>>

    var totalRows: Int

    var isError: Boolean

    var errorMessage: String

    var filter: FilterContainer

    var columns: List<Column>

    var sql: String

    var last: String

    fun setDuration(starttime: Long, endtime: Long)

    fun start()

    fun end()

    /**
     * Remove large data to generate a smaller response
     */
    fun minimize()

}
