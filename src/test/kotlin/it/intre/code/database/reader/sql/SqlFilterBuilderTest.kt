package it.intre.code.database.reader.sql

import it.intre.code.database.reader.config.Column
import it.intre.code.database.reader.config.QueryProfile
import it.intre.code.database.reader.filter.generic.ComparisonFilter
import it.intre.code.database.reader.filter.generic.GenericFilter
import it.intre.code.database.reader.filter.generic.TextFilter
import it.intre.code.database.reader.sql.SqlFilterBuilder.Companion.toFieldNameOrExpression
import it.intre.code.database.reader.sql.SqlHelper.TRUE
import it.intre.code.database.reader.sql.SqlMatcher.Companion.matchesSql
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

class SqlFilterBuilderTest {

    private val TEXT_ALL = TextFilter("", true, false, emptyList(), false)
    private val NUMBER_NEGATE_ALL = ComparisonFilter("", true, true, "A", "B", "C", "D", "E")
    private val TEXT_DERIVED = TextFilter("derived", false, false, listOf("one", "two"), false)
    private val INT_3_TO_10_EXCLUDED = ComparisonFilter("I", datatype = ("Integer"), gt = 3, lt = 10)
    private val STRING_A_INCLUDED_TO_Z_EXCLUDED = ComparisonFilter("S", ge = ("A"), lt = ("Z"))
    private val STRING_10_TO_2_INCLUDED_NEGATE = ComparisonFilter("X", datatype = ("String"), ge = 10, le = 2, negate = (true))
    private val INT_ALL_CONDITIONS = ComparisonFilter("N", datatype = ("Integer"), ge = 1, gt = 2, le = 3, lt = 4)
    private val INT_ONE_CONDITION = ComparisonFilter("Y", datatype = ("Integer"), le = 42)
    private val NO_CONDITIONS = ComparisonFilter("this should not be used")
    private val DERIVED_COLUMN_PROFILE = mockProfileWithDerivedColumns("I", "S", "X", "N", "Y")

    @Test
    fun factoryReturnsCorrectBuilder() {
        assertThat(SqlFilterBuilder.from(TEXT_ALL, null), instanceOf<Any>(TextFilterBuilder::class.java))

        assertThat(SqlFilterBuilder.from(NUMBER_NEGATE_ALL, null), instanceOf<Any>(ComparisonFilterBuilder::class.java))
    }

    @Test
    fun toSqlAllValues() {
        assertBuilds(TEXT_ALL, INCLUDE_ALL, null)
    }

    @Test
    fun toSqlNegateAll() {
        assertBuilds(NUMBER_NEGATE_ALL, EXCLUDE_ALL, null)
    }

    @Test
    fun toSqlDerivedField() {
        val qp = mockProfileWithDerivedColumns("derived")

        val expected = "( " +
                "UPPER(" + DERIVED_FIELD_COMPUTATION + ")='ONE' " +
                "OR " +
                "UPPER(" + DERIVED_FIELD_COMPUTATION + ")='TWO' " +
                ")"
        assertBuilds(TEXT_DERIVED, expected, qp)
    }

    @Test
    fun toFieldNameOrExpressionTest() {
        val qp = mockProfileWithDerivedColumns("derived")

        assertEquals(DERIVED_FIELD_COMPUTATION, toFieldNameOrExpression("derived", qp))
        assertEquals("POTATO", toFieldNameOrExpression("POTATO", qp))

        assertEquals("derived", toFieldNameOrExpression("derived", null))
        assertEquals("derived", toFieldNameOrExpression("derived", QueryProfile()))
    }

    @Test
    fun integerOpenInterval() {
        assertBuilds(INT_3_TO_10_EXCLUDED, "(I > 3 AND I < 10)", null)
    }

    @Test
    fun stringSemiOpenInterval() {
        assertBuilds(STRING_A_INCLUDED_TO_Z_EXCLUDED, "(S >= 'A' AND S < 'Z')", null)
    }

    @Test
    fun stringWithNumbersClosedInterval() {
        assertBuilds(STRING_10_TO_2_INCLUDED_NEGATE, " NOT (X >= '10' AND X <= '2') ", null)
    }

    @Test
    fun stringWithNumbersAllConditions() {
        assertBuilds(INT_ALL_CONDITIONS, "(N >= 1 AND N > 2 AND N <= 3 AND N < 4)", null)
    }

    @Test
    fun stringWithNumbersOneCondition() {
        assertBuilds(INT_ONE_CONDITION, "(Y <= 42)", null)
    }

    @Test
    fun noConditions() {
        assertBuilds(NO_CONDITIONS, "($TRUE)", null)
    }

    @Test
    fun stringWithNumbersAllConditionsDerived() {
        val expected = String.format("(%1\$s >= 1 AND %1\$s > 2 AND %1\$s <= 3 AND %1\$s < 4)", DERIVED_FIELD_COMPUTATION)
        assertBuilds(INT_ALL_CONDITIONS, expected, DERIVED_COLUMN_PROFILE)
    }

    companion object {

        internal val INCLUDE_ALL = "($TRUE)"
        internal val EXCLUDE_ALL = "NOT $INCLUDE_ALL"
        internal const val DERIVED_FIELD_COMPUTATION = "NVL(a,b)"

        internal fun mockProfileWithDerivedColumns(vararg names: String): QueryProfile {
            val columns = ArrayList<Column>()
            for (name in names) {
                val column = Column()
                column.alias = name
                column.name = DERIVED_FIELD_COMPUTATION
                column.isDerived = true
                columns.add(column)
            }
            val qp = QueryProfile()
            qp.columns = columns
            return qp
        }

        internal fun assertBuilds(filter: GenericFilter, expected: String, queryProfile: QueryProfile?) {
            val builder = SqlFilterBuilder.from(filter, queryProfile)
            val actual = builder.toSql()
            // check removing spaces: it's an implementation detail
            assertThat(actual, matchesSql(expected))
        }
    }
}