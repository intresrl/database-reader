package it.intre.code.database.reader.filter

import it.intre.code.database.reader.TestArguments
import it.intre.code.database.reader.config.Column
import it.intre.code.database.reader.sql.SqlMatcher
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.Arrays.asList
import java.util.stream.Stream

class QueryStringFilterTest {

    private var filter: QueryStringFilter = QueryStringFilter()

    @BeforeEach
    fun setUp() {
        filter.groupBy = asList("G-1", "G-2", "G-3")
    }

    @Test
    fun isContainedInGroup() {
        assertTrue(filter.isContainedInGroup(createColumn("G-1")))
        assertTrue(filter.isContainedInGroup(createColumn("G-2")))
        assertTrue(filter.isContainedInGroup(createColumn("G-3", "any_alias")))
        assertFalse(filter.isContainedInGroup(createColumn("not_there")))
        assertFalse(filter.isContainedInGroup(createColumn("A", "B")))
        assertFalse(filter.isContainedInGroup(createColumn("name_does_not_match", "G-3")))
    }

    private fun createColumn(name: String, alias: String = ""): Column {
        return Column().apply { this.name = name; this.alias = alias }
    }

    @Test
    fun getGroupAsString() {
        assertThat(filter.getGroupAsString("XYZ"), SqlMatcher.matchesSql("G-1 , G-2 , G-3"))
        assertThat(filter.getGroupAsString("G-2"), SqlMatcher.matchesSql("G-1 , G-3"))
    }

    @Test
    fun getGroupAsStringEmpty() {
        filter.groupBy = emptyList()
        assertThat(filter.getGroupAsString(null), SqlMatcher.matchesSql(""))
    }

    companion object {
        @JvmStatic
        fun getArguments(): Stream<Arguments> = listOf<Arguments>(
                TestArguments.of(listOf<OrderField>(), listOf<String>(), listOf<String>()),
                TestArguments.of(listOf(
                        OrderField("A", OrderField.ASC),
                        OrderField("B", OrderField.DESC),
                        OrderField("C", OrderField.ASC)
                ), listOf("A", "B", "C"), listOf(OrderField.ASC, OrderField.DESC, OrderField.ASC)),
                TestArguments.of(listOf(
                        OrderField("A", OrderField.ASC)
                ), listOf("A"), listOf(OrderField.ASC, OrderField.DESC, OrderField.ASC)),
                TestArguments.of(listOf(
                        OrderField("A", OrderField.ASC),
                        OrderField("B", OrderField.ASC),
                        OrderField("C", OrderField.ASC)
                ), listOf("A", "B", "C"), listOf(OrderField.ASC))
        ).stream()
    }

    @ParameterizedTest(name = "{1}+{2}->{0}")
    @MethodSource("getArguments")
    fun `Order field joins name and direction`(expected: List<OrderField>, orderBy: List<String>, orderDir: List<String>) {
        val queryStringFilter = QueryStringFilter(orderBy = orderBy, orderDir = orderDir)
        assertEquals(expected, queryStringFilter.orderFields)
    }

    @Test
    fun hasGroupBy() {
        assertFalse(QueryStringFilter().hasGroupBy())
        assertTrue(QueryStringFilter(groupBy = listOf("A")).hasGroupBy())
    }
}
