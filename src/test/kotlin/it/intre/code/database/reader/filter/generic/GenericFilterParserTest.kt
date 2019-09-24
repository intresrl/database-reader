package it.intre.code.database.reader.filter.generic

import it.intre.code.database.reader.filter.generic.GenericFilterParser.Companion.PREFIX
import org.hamcrest.CoreMatchers.hasItem
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import javax.ws.rs.core.MultivaluedHashMap
import javax.ws.rs.core.MultivaluedMap

class GenericFilterParserTest {

    private var params: MultivaluedMap<String, String> = MultivaluedHashMap<String, String>()

    @Test
    fun ignoreUnprefixed() {
        params.add("lorem", "lorem")
        params.add("ipsum", "ipsum")
        params.add("dolor", "dolor")

        val filters = doParse()
        assertEquals(0, filters.size)
    }

    @Test
    fun mixedConditionsOnDifferentFields() {
        params.add("f.text", "something*")
        params.add("f.text.negate", "true")
        params.add("f.num.gt", "2")

        val filters = doParse()
        assertEquals(2, filters.size)
        assertThat(filters, hasItem(instanceOf<Any>(TextFilter::class.java)))
        assertThat(filters, hasItem(instanceOf<Any>(ComparisonFilter::class.java)))
    }

    @Test
    fun correctSuffix() {
        params.add("f.text", "example")
        params.add("f.text.blank", "true")
        params.add("f.text.negate", "false") // this is right
        params.add("f.text.not", "true") // this is wrong, will be ignored

        val filters = doParse()
        assertEquals(1, filters.size)
        assertThat(filters, hasItem(TextFilter("text", false, false, listOf("example"), true)))
    }

    @Test
    fun mixedConditionsOnSameFieldMakeComparisonFilter() {
        params.add("f.bob", "should be ignored")
        params.add("f.bob.negate", "true")
        params.add("f.bob.gt", "2")

        val filters = doParse()
        assertEquals(1, filters.size)
        assertThat(filters, hasItem(ComparisonFilter("bob", false, true, "2", null, null, null, null)))
    }

    @Test
    fun testNoPrefix() {
        val s = "the pen is on the table"
        assertEquals(s, GenericFilterParser.noPrefix(PREFIX + s))
        assertEquals(s, GenericFilterParser.noPrefix(s))
    }

    @Test
    fun testNoSuffix() {
        var s = PREFIX + "the pen is on the table"
        assertEquals(s, GenericFilterParser.noSuffix("$s.potato"))
        assertEquals(s, GenericFilterParser.noSuffix(s))
        s = "lorem ipsum"
        assertEquals(s, GenericFilterParser.noSuffix("$s.dolor"))
        assertEquals(s, GenericFilterParser.noSuffix(s))
    }

    private fun doParse(): List<GenericFilter> {
        return GenericFilterParser(params).parse()
    }
}