package it.intre.code.database.reader.filter.generic

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ParseHandlerTest {
    @Test
    fun testToBoolean() {
        assertTrue(toBoolean("true"))
        assertTrue(toBoolean("TRUE"))
        assertTrue(toBoolean("True", "tRuE", "TRue"))
        assertFalse(toBoolean("false"))
        assertFalse(toBoolean("FALSE", "true"))
        assertFalse(toBoolean("a", "c;", "DXS"))
        assertFalse(toBoolean())
        assertFalse(toBoolean(null, "true"))
    }

    private fun toBoolean(vararg s: String?): Boolean {
        return ParseHandler.toBoolean(listOf(*s))
    }

}