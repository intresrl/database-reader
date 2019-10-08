package it.intre.code.database.reader.dao

import it.intre.code.database.reader.config.ProfileLoader
import it.intre.code.database.reader.config.ProfileUrlResolver
import it.intre.code.database.reader.config.ReadProfile
import it.intre.code.database.reader.filter.FilterContainer
import it.intre.code.database.reader.resultset.ReaderResultSet
import it.intre.code.database.reader.sql.SqlBuilder
import it.intre.code.database.reader.sql.SqlBuilder.Companion.ROWCOUNT_FIELD
import it.intre.code.database.reader.util.SqlDateManager
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import java.lang.Integer.parseInt
import java.lang.String.format
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.sql.SQLException
import java.sql.Statement
import java.util.*
import java.util.Collections.emptyList

/**
 * Database-specific implementation of the generic Data Access Object
 *
 * @author Giulio Roggero, Intré
 */
class ReaderDaoImpl : ReaderDao {

    private val logger = LoggerFactory.getLogger(ReaderDaoImpl::class.java)

    private lateinit var profileLoader: ProfileLoader

    private lateinit var genericDataSource: GenericDataSource

    private lateinit var profileUrlResolver: ProfileUrlResolver

    companion object {
        private val FETCH_SIZE = 500
        private val TIMESTAMP_STR = "timestamp"
    }

    override fun find(filter: FilterContainer): ReaderResultSet {

        val sql: String
        val sqlCount: String

        var result = ReaderResultSet()
        try {
            val profile = profileLoader.loadProfile(filter) { profileUrlResolver.toUrl(it) }
                    ?: throw Exception("Profile not found")
            sql = SqlBuilder.buildSql(filter, profile)
            sqlCount = SqlBuilder.buildCountSql(filter, profile)
            if (mustCalculateLast(filter)) {
                val sqlLast = getLastRecordQuery(filter, profile)
                result = _executeSql(filter, sqlLast, null)
                setLastValueFromList(result, filter.queryStringFilter.last!!)
                result.minimize()
            } else {
                result = _executeSql(filter, sql, sqlCount)
            }
        } catch (ex: Exception) {
            logger.warn("Error in find", ex)
            result.isError = true
            val msg = if (ex.message == null) Arrays.toString(ex.stackTrace) else ex.message
            result.errorMessage = msg
        }

        return result
    }

    /**
     * Put the (only) value in list and use it to set [ReaderResultSet.setLast];
     * replace list with an empty list. Return the same instance modified.
     */
    private fun setLastValueFromList(resultSet: ReaderResultSet, lastFieldName: String) {
        var last: String?
        try {
            val map = resultSet.list!!.first()
            last = map[lastFieldName].toString()
        } catch (e: Exception) {
            last = null
        }

        resultSet.last = last
        resultSet.list = emptyList()
    }

    override fun getColumns(filter: FilterContainer): ReaderResultSet {
        val result = ReaderResultSet()
        try {
            val profile = profileLoader.loadProfile(filter) { profileUrlResolver.toUrl(it) }
                    ?: throw Exception("Profile not found")
            result.columns = profile.columns
        } catch (ex: Exception) {
            logger.warn("Error in getColumns", ex)
            result.isError = true
            val msg = if (ex.message == null) Arrays.toString(ex.stackTrace) else ex.message
            result.errorMessage = msg
        }

        return result
    }

    private fun _executeSql(filter: FilterContainer, sql: String, sqlCount: String?): ReaderResultSet {
        logger.info(format("Executing %s", sql))
        val isLast = mustCalculateLast(filter)
        var profile: ReadProfile? = null
        val result = ReaderResultSet()
        result.start()

        var list: List<Map<String, Any>> = ArrayList()

        var numOfRows: Int
        try {
            genericDataSource.connection.use { connection ->
                connection.createStatement().use { stm ->
                    profile = profileLoader.loadProfile(filter) { profileUrlResolver.toUrl(it) }
                    result.sql = sql
                    stm.fetchSize = FETCH_SIZE
                    list = _executeSqlQuery(stm, sql, profile, isLast)
                    numOfRows = _getNumOfRows(stm, sqlCount, list, isLast)!!
                    result.isError = false
                    result.totalRows = numOfRows
                }
            }
        } catch (ex: Exception) {
            logger.error("Error in SQL execution", ex)
            result.isError = true
            val msg = if (ex.message == null) Arrays.toString(ex.stackTrace) else ex.message
            result.errorMessage = msg
        }

        result.end()
        result.filter = filter
        result.list = list
        if (profile != null) {
            result.columns = profile!!.columns
        }
        logger.info(format("Query found %s results in %s ms", list.size, result.durationMilliSeconds))
        return result
    }

    @Throws(SQLException::class)
    private fun _getNumOfRows(stm: Statement, sqlCount: String?, list: List<Map<String, Any>>, isLast: Boolean): Int? {
        try {
            if (isLast) return parseInt(list.first()[ROWCOUNT_FIELD].toString())
        } catch (e: Exception) {
            logger.warn("Error parsing number of rows from $list", e)
        }

        return _executeSqlCountQuery(stm, sqlCount)
    }

    @Throws(SQLException::class)
    private fun _executeSqlQuery(stm: Statement, sql: String, profile: ReadProfile?, isLast: Boolean): List<Map<String, Any>> {
        stm.executeQuery(sql).use { rs ->
            val rsmd = rs.metaData
            return _parseResult(rs, rsmd, profile, isLast)
        }
    }

    @Throws(SQLException::class)
    private fun _executeSqlCountQuery(stm: Statement, sql: String?): Int? {
        if (StringUtils.isBlank(sql)) {
            return -1
        }
        stm.executeQuery(sql).use { rs ->
            rs.next()
            return rs.getInt(1)
        }
    }

    @Throws(SQLException::class)
    private fun _parseResult(rs: ResultSet, meta: ResultSetMetaData, profile: ReadProfile?, isLast: Boolean): List<Map<String, Any>> {
        val list = ArrayList<Map<String, Any>>()

        while (rs.next()) {
            val m = HashMap<String, Any>()
            val columnCount = meta.columnCount

            // The column count starts from 1
            // the result is not sorted and getObject(string) doesn't work...
            for (i in 1..columnCount) {
                val name = meta.getColumnName(i)
                m[name.toLowerCase()] = rs.getObject(i)
            }

            if (isLast || profile == null) {
                list.add(m)
            } else {
                // sort the map by profile
                val sortedMap = LinkedHashMap<String, Any>()
                for (col in profile.columns ?: emptyList()) {
                    val colName = col.outName.toLowerCase()
                    if (m.containsKey(colName)) {
                        _formatResult(m[colName], col.formatSource)?.let{
                            sortedMap[colName] = it
                        }
                    }
                }
                list.add(sortedMap)
            }
        }

        return list
    }

    private fun _formatResult(anything: Any?, formatSource: String?): Any? {
        var res = anything
        if (formatSource != null && formatSource.isNotEmpty() && anything != null) {
            if (formatSource.equals(TIMESTAMP_STR, ignoreCase = true)) {
                res = SqlDateManager.fromTimestampToString(anything)
            } else {
                res = SqlDateManager.getFormatterFromFormatString(formatSource).format(anything as Date?)
            }
        }
        return res
    }

    private fun mustCalculateLast(filter: FilterContainer) = !filter.queryStringFilter.last.isNullOrBlank()

    private fun getLastRecordQuery(filter: FilterContainer, profile: ReadProfile): String {
        val lastRecordField = filter.queryStringFilter.last ?: ""
        val builder = SqlBuilder()
        val qp = profile.queries?.firstOrNull()
        return qp?.let { builder.buildLastInsertedQuery(filter, it, lastRecordField) } ?: ""
    }

    override fun setProfileLoader(profileLoader: ProfileLoader) {
        this.profileLoader = profileLoader
    }

    override fun setGenericDataSource(genericDataSource: GenericDataSource) {
        this.genericDataSource = genericDataSource
    }

    override fun setProfileUrlResolver(profileUrlResolver: ProfileUrlResolver) {
        this.profileUrlResolver = profileUrlResolver
    }
}
