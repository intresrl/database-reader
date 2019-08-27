package it.intre.code.database.reader.filter

import it.intre.code.database.reader.config.Column
import it.intre.code.database.reader.sql.SqlMatcher
import org.junit.Assert.assertThat
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Arrays.asList

class QueryStringFilterTest {

    private var filter: QueryStringFilter  = QueryStringFilter()

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
        return Column().apply { this.name=name; this.alias = alias}
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
}