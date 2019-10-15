package it.intre.code.database.reader.sql

import it.intre.code.database.reader.config.Column
import it.intre.code.database.reader.filter.FilterContainer
import it.intre.code.database.reader.filter.OrderField
import it.intre.code.database.reader.filter.QueryStringFilter
import it.intre.code.database.reader.filter.generic.ComparisonFilter
import it.intre.code.database.reader.filter.generic.TextFilter
import it.intre.code.database.reader.sql.SqlHelperTest.Companion.createQueryProfile
import it.intre.code.database.reader.sql.SqlMatcher.Companion.matchesSql
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import javax.ws.rs.core.MultivaluedHashMap

class SqlBuilderTest {

    @Test
    fun buildQuery() {
        val sqlBuilder = SqlBuilder()

        val filterContainer = makeFilterContainerWithOrder()
        val queryStringFilter = filterContainer.queryStringFilter
        queryStringFilter.page = (4)
        queryStringFilter.size = (15)
        queryStringFilter.reportFields = (listOf("the_name1", "the_name3"))

        val queryProfile = createQueryProfile()

        val sql = sqlBuilder.buildQuery(filterContainer, queryProfile)
        assertThat(sql, matchesSql("""
                SELECT the_alias1 , the_name3 FROM
                ( 
                  SELECT the_alias1 , the_name3, rownum AS rnum FROM
                  (
                    SELECT the_name1 AS the_alias1, the_name3 FROM TABLE_NAME
                    WHERE 1=1 
                    ORDER BY the_name1 ASC, the_name2 DESC, the_name3 ASC
                  ) 
                  WHERE rownum <= 75 
                )
                WHERE rnum > 60
                """
        ))
    }

    @Test
    fun buildQueryWithEmptyFilterSQL() {
        val sqlBuilder = SqlBuilder()

        val filterContainer = makeFilterContainerWithOrder()
        val queryStringFilter = filterContainer.queryStringFilter
        queryStringFilter.page = (5)
        queryStringFilter.size = (80)
        queryStringFilter.reportFields = (listOf("the_name1", "the_name3"))

        val queryProfile = createQueryProfile()

        val sql = sqlBuilder.buildQuery(filterContainer, queryProfile)
        assertThat(sql, matchesSql("""
                SELECT the_alias1, the_name3 FROM ( 
                  SELECT the_alias1, the_name3, rownum AS rnum FROM ( 
                    select the_name1 AS the_alias1, the_name3 from TABLE_NAME where 1=1 order by the_name1 ASC, the_name2 DESC, the_name3 ASC
                  ) WHERE rownum <= ${5 * 80 + 80}
                ) WHERE rnum > ${5 * 80}
                """
        ))
    }

    @Test
    fun buildQueryWithEmptyRequestAndNoPagination() {
        val sqlBuilder = SqlBuilder()

        val filterContainer = FilterContainer()

        val queryProfile = createQueryProfile()

        val sql = sqlBuilder.buildQuery(filterContainer, queryProfile)
        assertThat(sql, matchesSql(
                "select the_name1 as the_alias1, the_name2 as the_alias2, the_name3 from TABLE_NAME where 1=1 "
        ))
    }

    @Test
    fun buildQueryCount() {
        val sqlBuilder = SqlBuilder()
        sqlBuilder.setIsCount(true)

        val filterContainer = FilterContainer()

        val queryProfile = createQueryProfile()

        val sql = sqlBuilder.buildQuery(filterContainer, queryProfile)
        assertThat(sql, matchesSql("select count (*) as rowcount from ( select *  from table_name where 1=1  ) "))
    }

    @Test
    fun buildQueryWithAggregateFunction() {
        val sqlBuilder = SqlBuilder()

        val filterContainer = FilterContainer()

        val queryProfile = createQueryProfile("THE_TABLE", listOf(
                createColumn("sum(field1)", "number", "alias1", true, false),
                createPlainColumn("the_field2", "string", "alias2"),
                createPlainColumn("the_field3", "string", "alias3")
        ))
        filterContainer.queryStringFilter.groupBy = (listOf("the_field2", "the_field3"))

        val sql = sqlBuilder.buildQuery(filterContainer, queryProfile)
        assertThat(sql, matchesSql("""
                select sum(field1) as alias1, the_field2 as alias2, the_field3 as alias3
                from THE_TABLE where 1=1 group by the_field2, the_field3
                """
        ))
    }

    @Test
    fun buildQueryWithDerivedFieldsAndPagination() {
        val sqlBuilder = SqlBuilder()

        val filterContainer = FilterContainer()

        val derivedValue = "NVL(the_field2, the_field1||'foobar')"
        val queryProfile = createQueryProfile("THE_TABLE", listOf(
                createPlainColumn("the_field1", "number", "alias1"),
                createPlainColumn("the_field2", "string", "alias2"),
                createColumn(derivedValue, "string", "alias3", false, true)
        ))
        val queryStringFilter = filterContainer.queryStringFilter
        queryStringFilter.page = 4
        queryStringFilter.size = 15

        val sql = sqlBuilder.buildQuery(filterContainer, queryProfile)
        assertThat(sql, matchesSql("""
                SELECT the_field1 as alias1, the_field2 as alias2, $derivedValue as alias3 FROM(
                  SELECT the_field1,the_field2, rownum AS rnum FROM(
                    SELECT the_field1, the_field2
                    FROM THE_TABLE where 1=1
                  )
                  WHERE rownum <= 75 
                ) WHERE rnum > 60
                """
        ))
    }

    @Test
    fun buildQueryWithOtherTableName() {
        val sqlBuilder = SqlBuilder()

        val queryProfile = createQueryProfile()
        queryProfile.table = "THE_PROFILE_TABLE"

        val filterContainer = FilterContainer()

        val sql = sqlBuilder.buildQuery(filterContainer, queryProfile)
        // use table from profile
        assertThat(sql, matchesSql("" +
                "select the_name1 AS the_alias1 , the_name2 AS the_alias2 , the_name3" +
                " from THE_PROFILE_TABLE where 1=1 "
        ))
    }

    @Test
    fun buildQueryWithWhereAndOrder() {
        val sqlBuilder = SqlBuilder()

        val queryProfile = createQueryProfile()
        queryProfile.where = ("bla")
        queryProfile.order = ("THE_ORDER_FIELD")

        val filterContainer = FilterContainer()

        val sql = sqlBuilder.buildQuery(filterContainer, queryProfile)
        // use table from profile
        assertThat(sql, matchesSql("" +
                "select the_name1 AS the_alias1 , the_name2 AS the_alias2 , the_name3" +
                " from TABLE_NAME where 1=1 AND bla order by THE_ORDER_FIELD"
        ))
    }

    @Test
    fun buildQueryWithCustomTextFilter() {
        val sqlBuilder = SqlBuilder()

        val filterContainer = FilterContainer()

        val queryProfile = createQueryProfile()
        filterContainer.customFilters = (listOf(TextFilter("X", false, true, listOf("Y"), false)))

        val sql = sqlBuilder.buildQuery(filterContainer, queryProfile)
        assertThat(sql, matchesSql("select the_name1 AS the_alias1 , the_name2 AS the_alias2 , the_name3" + " from TABLE_NAME where 1=1 and (not (upper(X)='Y') OR X IS NULL)"
        ))
    }

    @Test
    fun buildQueryWithCustomComparisonFilter() {
        val sqlBuilder = SqlBuilder()

        val filterContainer = FilterContainer()

        val queryProfile = createQueryProfile()
        val cf = ComparisonFilter("X")
        cf.gt = ("666")
        filterContainer.customFilters = (listOf<ComparisonFilter>(cf))
        filterContainer.queryStringFilter.reportFields = (listOf("the_name3", "the_name2"))

        val sql = sqlBuilder.buildQuery(filterContainer, queryProfile)
        assertThat(sql, matchesSql("" +
                "select the_name2 AS the_alias2 , the_name3" +
                " from TABLE_NAME where 1=1 and (X>'666')"
        ))
    }

    @Test
    fun buildQueryWithExtraWhere() {
        val sqlBuilder = SqlBuilder()

        val filterContainer = FilterContainer()
        val multiMap = MultivaluedHashMap<String, String>()
        multiMap.putSingle("x", "98765")
        multiMap.putSingle("y", "43210")
        filterContainer.parseFrom(multiMap)

        val queryProfile = createQueryProfile()
        queryProfile.extraWhere = mapOf(
                "x" to "<condition x ?>",
                "z" to "this will be ignored",
                "y" to "<condition y ?>"
        )

        val sql = sqlBuilder.buildQuery(filterContainer, queryProfile)
        assertThat(sql, matchesSql("" +
                "select the_name1 as the_alias1, the_name2 as the_alias2, the_name3 from TABLE_NAME where 1=1" +
                " AND <condition x 98765> AND <condition y 43210>"
        ))
    }

    @Test
    fun buildLastInsertedQuery() {
        val sqlBuilder = SqlBuilder()

        val textFilter = TextFilter("C", false, false, listOf("4"), false)
        val filterContainer = FilterContainer()
        filterContainer.customFilters = (listOf(textFilter))
        val queryProfile = createQueryProfile()

        val sql = sqlBuilder.buildLastInsertedQuery(filterContainer, queryProfile, "POWER")
        assertThat(sql, matchesSql("" + "select MAX(POWER) AS power, COUNT(*) AS rowcount from TABLE_NAME where 1=1 AND (upper(C) = '4') "
        ))
    }

    private fun makeFilterContainerWithOrder(): FilterContainer {
        val filterContainer = FilterContainer()
        val qsf = QueryStringFilter()
        qsf.orderBy = (listOf("the_name1", "the_name2", "the_name3"))
        qsf.orderDir = (listOf(OrderField.ASC, OrderField.DESC, OrderField.ASC))
        filterContainer.queryStringFilter = (qsf)
        return filterContainer
    }

    companion object {

        internal fun createPlainColumn(name: String, type: String, alias: String) =
                createColumn(name, type, alias, aggregate = false, derived = false)

        internal fun createColumn(name: String, type: String, alias: String, aggregate: Boolean, derived: Boolean): Column {
            val c = Column()
            c.name = name
            c.type = type
            c.alias = alias
            c.isAggregate = aggregate
            c.isDerived = derived
            return c
        }
    }
}