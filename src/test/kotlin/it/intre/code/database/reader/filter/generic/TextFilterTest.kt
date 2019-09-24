package it.intre.code.database.reader.filter.generic

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TextFilterTest {
    private val all1 = TextFilter("X", true, false, EMPTY_LIST, true)
    private val all2 = TextFilter("X", true, false, EMPTY_LIST, false)
    private val all3 = TextFilter("X", true, false, LIST, true)
    private val all4 = TextFilter("X", true, false, LIST, false)
    private val none1 = TextFilter("X", true, true, EMPTY_LIST, true)
    private val none2 = TextFilter("X", true, true, EMPTY_LIST, false)
    private val none3 = TextFilter("X", true, true, LIST, true)
    private val none4 = TextFilter("X", true, true, LIST, false)
    private val negate1 = TextFilter("X", false, true, EMPTY_LIST, true)
    private val negate2 = TextFilter("X", false, true, EMPTY_LIST, false)
    private val negate3 = TextFilter("X", false, true, LIST, true)
    private val negate4 = TextFilter("X", false, true, LIST, false)
    private val noValues = TextFilter("X", false, false, EMPTY_LIST, false)
    private val someValues1 = TextFilter("X", false, false, EMPTY_LIST, true)
    private val someValues2 = TextFilter("X", false, false, LIST, true)
    private val someValues3 = TextFilter("X", false, false, LIST, false)

    @Test
    fun allTrueNegateFalseHasNoCondition() {
        assertFalse(all1.hasCondition())
        assertFalse(all2.hasCondition())
        assertFalse(all3.hasCondition())
        assertFalse(all4.hasCondition())
    }

    @Test
    fun allTrueNegateTrueHasCondition() {
        assertTrue(none1.hasCondition())
        assertTrue(none2.hasCondition())
        assertTrue(none3.hasCondition())
        assertTrue(none4.hasCondition())
    }

    @Test
    fun allFalseNegateTrueHasCondition() {
        assertTrue(negate1.hasCondition())
        assertTrue(negate2.hasCondition())
        assertTrue(negate3.hasCondition())
        assertTrue(negate4.hasCondition())
    }

    @Test
    fun allTrueNegateFalseNoValuesHasNoCondition() {
        assertFalse(noValues.hasCondition())
    }

    @Test
    fun allTrueNegateFalseWithValuesHasCondition() {
        assertTrue(someValues1.hasCondition())
        assertTrue(someValues2.hasCondition())
        assertTrue(someValues3.hasCondition())
    }

    companion object {

        private val EMPTY_LIST = emptyList<String>()
        private val LIST = listOf("a", "b")
    }

}
