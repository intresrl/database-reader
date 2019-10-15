package it.intre.code.database.reader.resultset

import it.intre.code.database.reader.config.Column
import it.intre.code.database.reader.filter.FilterContainer
import it.intre.code.database.reader.filter.QueryStringFilter

import java.util.Collections.emptyList

data class ReaderResultSet @JvmOverloads constructor(
        /**
         * Function returning current time stamp in nanosecond
         */
        val getNanoTime: () -> Long = System::nanoTime,
        /**
         * The [List] of records: each one is a map from field name (@[String]) to value (@[Object]).
         */
        var list: List<Map<String, Any>>? = emptyList(),

        /**
         * Total number of rows matching the filter (ignoring pagination).
         */
        var totalRows: Int = 0,
        /**
         * `true` if an error occurred.
         */
        var isError: Boolean = false,
        /**
         * The error message, if any.
         */
        var errorMessage: String? = null,
        /**
         * The applied filter
         */
        var filter: FilterContainer? = null,
        /**
         * The [Column]s in the read profile.
         */
        var columns: List<Column>? = null,
        /**
         * If [QueryStringFilter.last] is not blank,
         * this is the MAX value of that field with currently applied filters.
         */
        var last: String? = null
) {
    /**
     * The generated SQL.
     */
    var sql: String? = null

    private var starttime: Long = 0

    private var endtime: Long = 0

    val durationNanoSeconds: Long
        get() = this.endtime - this.starttime

    val durationMilliSeconds: Long
        get() = (this.endtime - this.starttime) / 1000000

    fun setDuration(starttime: Long, endtime: Long) {
        this.starttime = starttime
        this.endtime = endtime
    }

    fun start() {
        this.starttime = getNanoTime()
    }

    fun end() {
        this.endtime = getNanoTime()
    }

    /**
     * Remove large data to generate a smaller response
     */
    fun minimize() {
        this.sql = ""
        this.columns = null
        this.filter = null
    }
}
