package it.intre.code.database.reader.filter.generic


import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import javax.ws.rs.core.MultivaluedHashMap
import javax.ws.rs.core.MultivaluedMap


class ComparisonHandlerTest {
    private val params: MultivaluedMap<String, String> = MultivaluedHashMap()

    @Test
    fun noConditions() {
        val filter = doParse("noparams")
        assertFalse(filter.hasCondition())
    }

    @Test
    fun oneValue() {
        params.add("f.foo.gt", "1")
        val filter = doParse("f.foo")
        assertTrue(filter.hasCondition())
        assertEquals(ComparisonFilter(name = "foo", gt = "1"), filter)
    }

    @Test
    fun allValues() {
        params.add("f.bar.gt", "42")
        params.add("f.bar.ge", "43")
        params.add("f.bar.le", "44")
        params.add("f.bar.lt", "45")
        val filter = doParse("f.bar")
        assertTrue(filter.hasCondition())
        assertEquals(ComparisonFilter(name = "bar", gt = "42", ge = "43", le = "44", lt = "45"), filter)
    }

    @Test
    fun withDatatype() {
        params.add("f.baz.lt", "47")
        params.add("f.baz.datatype", "Integer")
        val filter = doParse("f.baz")
        assertTrue(filter.hasCondition())
        assertEquals(ComparisonFilter(name = "baz", lt = "47", datatype = "Integer"), filter)
    }

    @Test
    fun negate() {
        params.add("f.fizz.ge", "10")
        params.add("f.fizz.le", "99")
        params.add("f.fizz.datatype", "string")
        params.add("f.fizz.negate", "true")
        params.add("f.fizz.negate", "TRUE")
        params.add("f.fizz.all", "?")
        val filter = doParse("f.fizz")
        assertTrue(filter.hasCondition())

        val expected = ComparisonFilter(ge = "10", le = "99", datatype = "string", negate = true, name = "fizz", all = false)
        assertEquals(expected, filter)
    }

    private fun doParse(name: String): GenericFilter {
        return ComparisonHandler(params).toFilter(name)
    }

}