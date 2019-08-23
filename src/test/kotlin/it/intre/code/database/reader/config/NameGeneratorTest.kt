package it.intre.code.database.reader.config

import org.junit.Before
import org.junit.Test

import it.intre.code.database.reader.config.Column.NameGenerator.*
import org.junit.Assert.assertEquals

class NameGeneratorTest {

    private var column: Column? = null

    @Before
    @Throws(Exception::class)
    fun setUp() {
        column = ColumnTest.createColumn("ETA", "BETA")
    }

    @Test
    fun nameGenerator_ONLY_NAME_NO_DERIVED_for_derived_column() {
        column!!.isDerived = true
        assertEquals("", ONLY_NAME_NO_DERIVED.generate(column!!))
    }

    @Test
    fun nameGenerator_ONLY_NAME_NO_DERIVED_for_standard_column() {
        assertEquals("ETA", ONLY_NAME_NO_DERIVED.generate(column!!))
    }

    @Test
    fun nameGenerator_ONLY_ALIAS() {
        assertEquals("BETA", ONLY_ALIAS.generate(column!!))
    }

    @Test
    fun nameGenerator_NAME_WITH_ALIAS() {
        assertEquals("ETA AS BETA", NAME_WITH_ALIAS.generate(column!!))
    }
}