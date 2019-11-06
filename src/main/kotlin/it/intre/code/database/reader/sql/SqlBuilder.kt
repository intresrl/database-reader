package it.intre.code.database.reader.sql

import it.intre.code.database.reader.config.QueryProfile
import it.intre.code.database.reader.config.ReadProfile
import it.intre.code.database.reader.filter.FilterContainer

import org.slf4j.LoggerFactory

class SqlBuilder {

    private val logger = LoggerFactory.getLogger(javaClass)

    private var isCount = false

    internal fun buildQuery(filter: FilterContainer, queryProfile: QueryProfile): String {
        queryProfile.resolveMacro(filter)
        val sql = _appendSqlConditions(filter, queryProfile)
        logger.info(sql)
        return sql
    }

    private fun _appendSqlConditions(filter: FilterContainer, queryProfile: QueryProfile): String {
        var clauses = " from " + queryProfile.table
        clauses += SqlHelper.startWhere()
        clauses += SqlHelper.andOrFilter(filter.customFilters, queryProfile)
        clauses = _addCustomWhere(clauses, queryProfile)

        val groupBy = SqlHelper.groupBy(filter)
        clauses = _addCustomGroupBy(clauses, groupBy, queryProfile)
        clauses = _addCustomHaving(clauses, queryProfile)

        val sql: String
        if (this.isCount) {
            sql = " select count(*) as $ROWCOUNT_FIELD from ( select * $clauses ) "
        } else {
            clauses += SqlHelper.orderBy(filter, queryProfile)
            clauses = _addCustomOrder(clauses, queryProfile)
            clauses = _addMaxRecords(clauses, queryProfile)
            sql = SqlHelper.paginationInnerQuery(filter, queryProfile, clauses)
        }

        return sql
    }

    private fun _addCustomOrder(sql: String, queryProfile: QueryProfile): String {
        var sqlString = sql
        if (queryProfile.isOrder) {
            sqlString +=
                    if (!sqlString.toLowerCase().contains("order by")) {
                        " ORDER BY "
                    } else {
                        ", "
                    }
            sqlString += queryProfile.order
        }
        return sqlString
    }

    private fun _addCustomHaving(sql: String, queryProfile: QueryProfile): String {
        var sqlString = sql
        if (queryProfile.isHaving) {
            sqlString += " HAVING " + queryProfile.having
        }
        return sqlString
    }

    private fun _addCustomGroupBy(sql: String, groupBy: String, queryProfile: QueryProfile): String {
        var sqlString = sql
        sqlString += groupBy
        if (queryProfile.isGroup) {
            sqlString +=
                    if (groupBy.isEmpty()) {
                        " GROUP BY "
                    } else {
                        ","
                    }
            sqlString += queryProfile.group
        }
        return sqlString
    }

    private fun _addCustomWhere(sql: String, queryProfile: QueryProfile): String {
        val b = StringBuilder(sql)
        if (queryProfile.isWhere) {
            b.append(" AND ").append(queryProfile.where)
        }
        for (condition in queryProfile.extraWhere?.values ?: listOf()) {
            b.append(" AND ").append(condition)
        }
        return b.toString()
    }

    private fun _addMaxRecords(sql: String, queryProfile: QueryProfile): String {
        var sqlString = sql
        if (queryProfile.isMaxRecords) {
            //FIXME: depends on the driver - this implementation is valid for oracle 12c
            sqlString += " OFFSET 0 ROWS FETCH NEXT ${queryProfile.maxRecords} ROWS ONLY"
        }
        return sqlString
    }

    internal fun setIsCount(isCount: Boolean?) {
        this.isCount = isCount!!
    }

    fun buildLastInsertedQuery(filter: FilterContainer, queryProfile: QueryProfile, lastRecordField: String): String {
        queryProfile.resolveMacro(filter)
        var sql = (""
                + "SELECT max(" + lastRecordField
                + ") as " + lastRecordField
                + ",COUNT(*) as " + ROWCOUNT_FIELD
                + " from " + queryProfile.table
                + SqlHelper.startWhere()
                + SqlHelper.andOrFilter(filter.customFilters, queryProfile))
        sql = _addCustomWhere(sql, queryProfile)
        return sql
    }

    companion object {

        val ROWCOUNT_FIELD = "rowcount"
        private val UNION_ALL = " UNION ALL "

        @Throws(Exception::class)
        fun buildCountSql(filter: FilterContainer, profile: ReadProfile): String {
            return _buildSql(filter, profile, true)
        }

        @Throws(Exception::class)
        fun buildSql(filter: FilterContainer, profile: ReadProfile): String {
            return _buildSql(filter, profile, false)
        }

        @Throws(Exception::class)
        private fun _buildSql(filter: FilterContainer, profile: ReadProfile, isCount: Boolean): String {

            val p = filter.profile
            if (p == null || p.isEmpty()) {
                throw Exception("No Sql provided")
            }

            return _buildQueryFromProfile(filter, profile, isCount)
        }

        private fun _buildQueryFromProfile(filter: FilterContainer, profile: ReadProfile, isCount: Boolean) =
                (profile.queries ?: emptyList())
                        .joinToString(separator = UNION_ALL) {
                            _buildSingleQueryStatement(filter, it, isCount)
                        }

        private fun _buildSingleQueryStatement(filter: FilterContainer, queryProfile: QueryProfile, isCount: Boolean): String {
            val builder = SqlBuilder()
            builder.setIsCount(isCount)
            return builder.buildQuery(filter, queryProfile)
        }
    }

}

