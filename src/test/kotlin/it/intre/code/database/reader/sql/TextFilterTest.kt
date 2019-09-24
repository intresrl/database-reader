package it.intre.code.database.reader.sql

import it.intre.code.database.reader.filter.generic.TextFilter
import it.intre.code.database.reader.sql.SqlFilterBuilderTest.Companion.DERIVED_FIELD_COMPUTATION
import it.intre.code.database.reader.sql.SqlFilterBuilderTest.Companion.EXCLUDE_ALL
import it.intre.code.database.reader.sql.SqlFilterBuilderTest.Companion.INCLUDE_ALL
import it.intre.code.database.reader.sql.SqlFilterBuilderTest.Companion.assertBuilds
import it.intre.code.database.reader.sql.SqlFilterBuilderTest.Companion.mockProfileWithDerivedColumns
import org.junit.jupiter.api.Test
import java.util.Collections.emptyList

class TextFilterTest {

    private val VOID = TextFilter("name", false, false, emptyList(), false)
    private val SIMPLE_ONE_VALUE = TextFilter("name", false, false, listOf("Alice"), false)
    private val SIMPLE_TWO_VALUES = TextFilter("name", false, false, listOf("bob", "ChArLiE"), false)
    private val WITH_WILDCARD = TextFilter("name", false, false, listOf("delta", "*echo", "fox%trot"), false)
    private val WITH_WILDCARD_ESCAPED = TextFilter("name", false, false, listOf("*li_ma*", "mi_ke"), false)
    private val WITH_BLANK = TextFilter("name", false, false, listOf("golf"), true)
    private val ALL_VALUES_SIMPLE = TextFilter("name", true, false, emptyList(), true)
    private val ALL_VALUES_WITH_EXPLICIT = TextFilter("name", true, false, listOf("X", "Y"), true)
    private val NEGATE = TextFilter("name", false, true, listOf("hotel", "lima"), false)
    private val NEGATE_WILDCARD = TextFilter("name", false, true, listOf("*india%"), false)
    private val NEGATE_BLANK = TextFilter("name", false, true, emptyList(), true)
    private val NEGATE_ALL = TextFilter("name", true, true, emptyList(), false)
    private val NEGATE_WILDCARD_BLANK = TextFilter("name", false, true, listOf("Juliett", "Ki*lo*"), true)

    @Test
    fun toSqlVoid() {
        assertBuilds(VOID, INCLUDE_ALL, null)
    }

    @Test
    fun toSqlSimpleOneValue() {
        assertBuilds(SIMPLE_ONE_VALUE, "(UPPER(name)='ALICE')", null)
    }

    @Test
    fun toSqlSimpleTwoValues() {
        assertBuilds(SIMPLE_TWO_VALUES, "( UPPER(name) = 'BOB' OR UPPER(name) = 'CHARLIE' )", null)
    }

    @Test
    fun toSqlWithWildcard() {
        assertBuilds(WITH_WILDCARD, "(UPPER(name)='DELTA' OR UPPER(name) LIKE '%ECHO'  ESCAPE '\\' OR UPPER(name) LIKE 'FOX%TROT'  ESCAPE '\\' )", null)
    }

    @Test
    fun toSqlWithWildcardEscaped() {
        assertBuilds(WITH_WILDCARD_ESCAPED, "(UPPER(name) LIKE '%LI\\_MA%' ESCAPE '\\' OR UPPER(name) ='MI_KE')", null)
    }

    @Test
    fun toSqlWithBlank() {
        assertBuilds(WITH_BLANK, "(name IS NULL OR UPPER(name)='GOLF')", null)
    }

    @Test
    fun toSqlAllValues() {
        assertBuildsForAnyValueOfBlank(ALL_VALUES_SIMPLE, INCLUDE_ALL)
    }

    @Test
    fun toSqlAllValuesWithExplicit() {
        assertBuildsForAnyValueOfBlank(ALL_VALUES_WITH_EXPLICIT, INCLUDE_ALL)
    }

    @Test
    fun toSqlNegate() {
        assertBuilds(NEGATE, "(NOT (upper(name) = 'HOTEL' OR upper(name) = 'LIMA') OR name IS NULL)", null)
    }

    @Test
    fun toSqlNegateWildcard() {
        assertBuilds(NEGATE_WILDCARD, "(NOT (upper(name) like '%india%' escape '\\') OR name IS NULL)", null)
    }

    @Test
    fun toSqlNegateBlank() {
        assertBuilds(NEGATE_BLANK, "NOT (NAME IS NULL)", null)
    }

    @Test
    fun toSqlNegateAll() {
        assertBuildsForAnyValueOfBlank(NEGATE_ALL, EXCLUDE_ALL)
    }

    @Test
    fun toSqlAllTogether() {
        assertBuilds(NEGATE_WILDCARD_BLANK, " NOT (NAME IS NULL OR UPPER(NAME)='JULIETT' OR UPPER(NAME) LIKE 'KI%LO%' ESCAPE '\\' )", null)
    }

    private fun assertBuildsForAnyValueOfBlank(filter: TextFilter, expected: String) {
        filter.withBlank = false
        assertBuilds(filter, expected, null)

        filter.withBlank = true
        assertBuilds(filter, expected, null)
    }

    @Test
    fun toSqlNegateWildcardWithDerivedFields() {
        val s = DERIVED_FIELD_COMPUTATION
        val expected = "( NOT (upper($s) like '%india%' escape '\\') OR $s IS NULL)"
        assertBuilds(NEGATE_WILDCARD, expected, mockProfileWithDerivedColumns("name"))
    }

}