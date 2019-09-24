package it.intre.code.database.reader.sql

import it.intre.code.database.reader.config.Column
import it.intre.code.database.reader.config.Column.NameGenerator.*
import it.intre.code.database.reader.config.QueryProfile
import it.intre.code.database.reader.filter.FilterContainer
import it.intre.code.database.reader.filter.OrderField.Companion.ASC
import it.intre.code.database.reader.filter.OrderField.Companion.DESC
import it.intre.code.database.reader.filter.QueryStringFilter
import it.intre.code.database.reader.sql.SqlHelper.Companion.computeRowRange
import it.intre.code.database.reader.sql.SqlHelper.Range
import it.intre.code.database.reader.sql.SqlMatcher.Companion.matchesSql
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import java.util.*

class SqlHelperTest {

    @Test
    fun noFieldsSpecified() {
        val filterContainer = FilterContainer()

        val queryProfile = createQueryProfile("TABLE_NAME", emptyList())

        val sql = SqlHelper.fields(filterContainer, queryProfile, NAME_WITH_ALIAS)

        assertThat(sql, matchesSql("*"))
    }

    @Test
    fun withOneField() {
        val filterContainer = FilterContainer()

        val queryStringFilter = filterContainer.queryStringFilter
        val reportFields = ArrayList<String>()
        reportFields.add("the_name1")
        queryStringFilter.reportFields = (reportFields)

        val queryProfile = createQueryProfile()

        val sql = SqlHelper.fields(filterContainer, queryProfile, NAME_WITH_ALIAS)

        assertThat(sql, matchesSql("the_name1 as the_alias1"))
    }

    @Test
    fun withMoreFields() {
        val filterContainer = FilterContainer()

        val queryProfile = createQueryProfile()

        val sql = SqlHelper.fields(filterContainer, queryProfile, NAME_WITH_ALIAS)

        assertThat(sql, matchesSql("the_name1 as the_alias1,the_name2 as the_alias2,the_name3"))
    }

    @Test
    fun withRedundantAliases() {
        val filterContainer = FilterContainer()

        val queryProfile = createQueryProfile()
        val columns = queryProfile.columns ?: emptyList()
        for (column in columns) {
            column.alias = column.name
        }
        val sql = SqlHelper.fields(filterContainer, queryProfile, NAME_WITH_ALIAS)

        assertThat(sql, matchesSql("the_name1,the_name2,the_name3"))
    }

    @Test
    fun onlyAliases() {
        val sql = SqlHelper.fields(FilterContainer(), createQueryProfile(), ONLY_ALIAS)
        assertThat(sql, matchesSql("the_alias1,the_alias2,the_name3"))
    }

    @Test
    fun onlyNameNoDerived() {
        val queryProfile = createQueryProfile("TABLE_NAME", listOf(
                createPlainColumn("the_name1", "string", "the_alias1"),
                createPlainColumn("the_name2", "string", ""),
                createColumn("FOO(the_name1)||BAR(the_name2)", "string", "derived", aggregate = false, derived = true)
        ))

        val sql = SqlHelper.fields(FilterContainer(), queryProfile, ONLY_NAME_NO_DERIVED)
        assertThat(sql, matchesSql("the_name1,the_name2"))
    }

    @Test
    fun withGroupBy() {
        val filterContainer = FilterContainer()
        filterContainer.queryStringFilter.groupBy = (listOf("the_name1"))

        val queryProfile = createQueryProfile()

        var sql = SqlHelper.fields(filterContainer, queryProfile, NAME_WITH_ALIAS)
        assertThat(sql, matchesSql("the_name1 as the_alias1"))

        sql = SqlHelper.groupBy(filterContainer)
        assertThat(sql, matchesSql("group by the_name1"))
    }

    @Test
    fun withAggregate() {
        val filterContainer = FilterContainer()
        filterContainer.queryStringFilter.groupBy = (listOf("the_field2", "the_field3"))

        val queryProfile = createQueryProfile("TABLE_NAME", listOf(
                createColumn(name = "sum(field1)", type = "number", alias = "alias1", aggregate = true, derived = false),
                createPlainColumn("the_field2", "string", "alias2"),
                createPlainColumn("the_field3", "string", "alias3")
        ))

        var sql = SqlHelper.fields(filterContainer, queryProfile, NAME_WITH_ALIAS)
        assertThat(sql, matchesSql("sum(field1) as alias1,the_field2 as alias2,the_field3 as alias3"))

        sql = SqlHelper.groupBy(filterContainer)
        assertThat(sql, matchesSql("group by the_field2,the_field3"))
    }

    @Test
    fun onlyFewFields() {
        val filterContainer = FilterContainer()
        filterContainer.queryStringFilter.reportFields = (listOf("the_name1", "the_name3"))

        val queryProfile = createQueryProfile()

        val sql = SqlHelper.fields(filterContainer, queryProfile, NAME_WITH_ALIAS)

        assertThat(sql, matchesSql(" the_name1  as the_alias1, the_name3"))
    }

    @Test
    fun orderBy() {
        val filterContainer = FilterContainer()
        setOrder(filterContainer, mapOf("the_name1" to ASC))
        val queryProfile = createQueryProfile()
        val sql = SqlHelper.orderBy(filterContainer, queryProfile)

        assertThat(sql, matchesSql("order by the_name1 ASC"))
    }

    @Test
    fun orderByMultiple() {
        val filterContainer = FilterContainer()
        setOrder(filterContainer, mapOf("the_name1" to ASC, "the_name2" to DESC, "the_name3" to ASC))
        val queryProfile = createQueryProfile()
        val sql = SqlHelper.orderBy(filterContainer, queryProfile)

        assertThat(sql, matchesSql(" order by the_name1 ASC, the_name2 DESC, the_name3 ASC"))
    }

    private fun setOrder(filterContainer: FilterContainer, orderFields: Map<String, String>) {
        val qsf = QueryStringFilter()
        qsf.orderBy = (ArrayList(orderFields.keys))
        qsf.orderDir = (ArrayList(orderFields.values))
        filterContainer.queryStringFilter = (qsf)
    }

    @Test
    fun pagination() {
        val filterContainer = FilterContainer()

        val queryStringFilter = filterContainer.queryStringFilter
        queryStringFilter.page = (9)
        queryStringFilter.size = (10)
        queryStringFilter.reportFields = (listOf("the_name1", "the_name3"))

        val queryProfile = createQueryProfile()
        val sql = SqlHelper.paginationInnerQuery(filterContainer, queryProfile, CLAUSES)

        assertThat(sql, matchesSql("" +
                "SELECT the_alias1, the_name3 FROM ( " +
                "SELECT the_alias1, the_name3, rownum AS rnum FROM (" +
                "SELECT the_name1 as the_alias1, the_name3 " + CLAUSES +
                ") WHERE rownum <=  " + (9 * 10 + 10) +
                ") WHERE rnum > " + 9 * 10))
    }

    @Test
    fun paginationWithDerived() {
        val filterContainer = FilterContainer()

        val derivedValue = "NVL(the_field2, the_field1||'foobar')"
        val queryProfile = createQueryProfile("THE_TABLE", listOf(
                createPlainColumn("the_field1", "number", "alias1"),
                createPlainColumn("the_field2", "string", "alias2"),
                createColumn(derivedValue, "string", "alias3", aggregate = false, derived = true)
        ))
        val queryStringFilter = filterContainer.queryStringFilter
        queryStringFilter.page = (4)
        queryStringFilter.size = (15)

        val fieldNames = "the_field1 as alias1,the_field2 as alias2," +
                derivedValue + " as alias3"
        val clauses = " WHERE something"
        val sql = SqlHelper.paginationInnerQuery(filterContainer, queryProfile, clauses)

        assertThat(sql, matchesSql("" +
                "SELECT " + fieldNames + " FROM ( " +
                " SELECT the_field1, the_field2, rownum AS rnum FROM ( " +
                "  SELECT the_field1, the_field2 " + clauses +
                " ) WHERE rownum <= 75" +
                ") WHERE rnum > 60"))
    }

    @Test
    fun paginationWithoutStartingPage() {
        val filterContainer = FilterContainer()

        val queryStringFilter = filterContainer.queryStringFilter
        queryStringFilter.size = 987
        queryStringFilter.reportFields = (listOf("the_name1", "the_name3"))

        val queryProfile = createQueryProfile()
        val sql = SqlHelper.paginationInnerQuery(filterContainer, queryProfile, CLAUSES)

        assertThat(sql, matchesSql("SELECT the_name1 as the_alias1, the_name3 $CLAUSES"))
    }

    @Test
    fun paginationWithoutSize() {
        val filterContainer = FilterContainer()

        val queryStringFilter = filterContainer.queryStringFilter
        queryStringFilter.page = (123456)
        queryStringFilter.reportFields = (listOf("the_name1", "the_name3"))

        val queryProfile = createQueryProfile()
        val sql = SqlHelper.paginationInnerQuery(filterContainer, queryProfile, CLAUSES)

        assertThat(sql, matchesSql("SELECT the_name1 as the_alias1, the_name3 $CLAUSES"))
    }

    @Test
    fun paginationWithPageRange() {
        val filterContainer = FilterContainer()

        val queryStringFilter = filterContainer.queryStringFilter
        queryStringFilter.pageFrom = (4)
        queryStringFilter.pageTo = (8)
        queryStringFilter.size = (10)
        queryStringFilter.reportFields = (listOf("the_name1", "the_name3"))

        val queryProfile = createQueryProfile()
        val sql = SqlHelper.paginationInnerQuery(filterContainer, queryProfile, CLAUSES)

        assertThat(sql, matchesSql("" +
                "SELECT the_alias1, the_name3 FROM ( " +
                "SELECT the_alias1, the_name3, rownum AS rnum FROM (" +
                "SELECT the_name1 as the_alias1, the_name3 " + CLAUSES +
                ") WHERE rownum <=  " + (8 * 10 + 10) +
                ") WHERE rnum > " + 4 * 10))
    }

    @Test
    fun paginationWithPageOverPageRange() {
        val filterContainer = FilterContainer()

        val queryStringFilter = filterContainer.queryStringFilter
        queryStringFilter.page = (9)
        queryStringFilter.pageFrom = (66)
        queryStringFilter.pageTo = (77)
        queryStringFilter.size = (10)
        queryStringFilter.reportFields = (listOf("the_name1", "the_name3"))

        val queryProfile = createQueryProfile()
        val sql = SqlHelper.paginationInnerQuery(filterContainer, queryProfile, CLAUSES)

        assertThat(sql, matchesSql("" +
                "SELECT the_alias1, the_name3 FROM ( " +
                "SELECT the_alias1, the_name3, rownum AS rnum FROM (" +
                "SELECT the_name1 as the_alias1, the_name3 " + CLAUSES +
                ") WHERE rownum <=  " + (9 * 10 + 10) +
                ") WHERE rnum > " + 9 * 10))
    }

    @Test
    fun normalizeSpaces() {
        val sql = "    lorem      ipsum\tdolor +    consecutur ( ? ) \n\n       sit /   amen  ' foo % '  bar"
        val actual = SqlHelper.normalizeSpaces(sql)
        val expected = "lorem ipsum dolor+consecutur(?)sit/amen'foo%'bar"
        assertEquals(expected, actual)
    }

    @Test
    fun testComputeRowRange() {
        assertEquals(
                Range(280, 350),
                computeRowRange(4, null, null, 70),
                "Use page"
        )
        assertEquals(
                Range(14, 16),
                computeRowRange(7, 999, 999, 2),
                "Page has priority"
        )
        try {
            computeRowRange(1, 2, 3, null)
            fail("Size must not be null")
        } catch (e: NullPointerException) {
            // the error is expected
        }

        assertEquals(
                Range(180, 639),
                computeRowRange(null, 20, 70, 9),
                "Use pageFrom / pageTo"
        )
        assertEquals(
                Range(+25, -45),
                computeRowRange(null, -5, +8, -5),
                "Nonsense, but negative numbers are allowed at the moment"
        )
        assertEquals(
                Range(0, 0),
                computeRowRange(326, null, null, 0),
                "Nonsense, but size = 0 is allowed at the moment"
        )
    }

    companion object {

        private val CLAUSES = "AAAA"

        fun createQueryProfile() =
                createQueryProfile("TABLE_NAME", listOf(
                        createPlainColumn("the_name1", "string", "the_alias1"),
                        createPlainColumn("the_name2", "string", "the_alias2"),
                        createPlainColumn("the_name3", "string", "")
                ))

        internal fun createQueryProfile(tableName: String, columns: List<Column>): QueryProfile {
            val queryProfile = QueryProfile()
            queryProfile.table = (tableName)
            queryProfile.columns = (columns)
            return queryProfile
        }

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