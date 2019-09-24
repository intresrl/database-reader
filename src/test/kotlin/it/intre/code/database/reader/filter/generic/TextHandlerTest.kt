package it.intre.code.database.reader.filter.generic


import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.Collections.emptyList
import javax.ws.rs.core.MultivaluedHashMap
import javax.ws.rs.core.MultivaluedMap

class TextHandlerTest {
    private val params: MultivaluedMap<String, String> = MultivaluedHashMap()

    @Test
    fun twoValues() {
        params.add("f.foo", "1")
        params.add("f.foo", "2")

        val filter = doParse("f.foo")
        assertTrue(filter.hasCondition())
        assertEquals(TextFilter("foo", false, false, listOf("1", "2"), false), filter)
    }

    @Test
    fun wildcards() {
        params.add("f.bar", "abc*")
        params.add("f.bis", "%x%")

        val filter1 = doParse("f.bar")
        assertTrue(filter1.hasCondition())
        assertEquals(TextFilter("bar", false, false, listOf("abc*"), false), filter1)

        val filter2 = doParse("f.bis")
        assertTrue(filter2.hasCondition())
        assertEquals(TextFilter("bis", false, false, listOf("%x%"), false), filter2)
    }

    @Test
    fun onlyWildcardsIsTheSameAsNoFilter() {
        params.add("f.bar", "*")
        params.add("f.bis", "%")

        val filter1 = doParse("f.bar")
        assertFalse(filter1.hasCondition())

        val filter2 = doParse("f.bis")
        assertFalse(filter2.hasCondition())
    }

    @Test
    fun negate() {
        params.add("f.descrizione", "*session*")
        params.add("f.descrizione.negate", "true")

        val filter = doParse("f.descrizione")
        assertTrue(filter.hasCondition())
        assertEquals(TextFilter("descrizione", false, true, listOf("*session*"), false), filter)
    }

    @Test
    fun negateWildcard() {
        params.add("f.baz", "xyz")
        params.add("f.baz.negate", "TRUE")

        val filter = doParse("f.baz")
        assertTrue(filter.hasCondition())
        assertEquals(TextFilter("baz", false, true, listOf("xyz"), false), filter)
    }

    @Test
    fun blank() {
        params.add("f.fizz", "0")
        params.add("f.fizz.blank", "True")

        val filter = doParse("f.fizz")
        assertTrue(filter.hasCondition())
        assertEquals(TextFilter("fizz", false, false, listOf("0"), true), filter)
    }

    @Test
    fun onlyOptions() {
        params.add("f.buzz.negate", "true")
        params.add("f.buzz.blank", "True")
        params.add("f.buzz.all", "false")

        val filter = doParse("f.buzz")
        assertTrue(filter.hasCondition())
        assertEquals(TextFilter("buzz", false, true, emptyList(), true), filter)
    }

    @Test
    fun anyNameIsValid() {
        params.add("lorem", "lorem")
        params.add("ipsum", "ipsum")
        params.add("dolor", "dolor")

        var filter = doParse("f.lorem")
        assertTrue(filter is TextFilter)
        assertFalse(filter.hasCondition())
        assertTrue((filter as TextFilter).values.isEmpty())

        filter = doParse("lorem")
        assertTrue(filter is TextFilter)
        assertTrue(filter.hasCondition())
        assertEquals(1, (filter as TextFilter).values.size)
    }

    @Test
    fun everything() {
        for (i in 0..9) {
            val name = "f.param$i"
            for (j in 1..i) {
                params.add(name, "Value-$j")
            }
            params.add("Ignore this", "#$i")
            if (i == 0)
                params.add(name + ParseHandler.ALL_SUFFIX, "true")
            if (i == 1)
                params.add(name + TextHandler.BLANK_SUFFIX, "true")
            if (i == 2)
                params.add(name + ParseHandler.NEGATE_SUFFIX, "true")
        }

        val list = mutableListOf<GenericFilter>()
        for (i in 0..9) {
            val f = doParse("f.param$i")
            assertNotNull(f)
            list.add(f)
        }

        //FIXME: non va... :(
//        assertThat(list, hasItems(
//                TextFilter("param0", true, false, emptyList(), false),
//                TextFilter("param1", false, false, listOf("Value-1"), true),
//                TextFilter("param2", false, true, listOf("Value-1", "Value-2"), false)
//        ))
    }

    private fun doParse(name: String): GenericFilter {
        return TextHandler(params).toFilter(name)
    }

}