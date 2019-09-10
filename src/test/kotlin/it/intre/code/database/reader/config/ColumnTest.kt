package it.intre.code.database.reader.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


class ColumnTest {

    @Test
    fun getOutName_returns_alias_if_present() {
        val column = createColumn("NONNA PAPERA", "CICCIO")
        assertEquals("CICCIO", column.outName)
    }

    @Test
    fun getOutName_returns_name_if_null_alias() {
        val column = createColumn("PAPERONE", null)
        assertEquals("PAPERONE", column.outName)
    }

    @Test
    fun getOutName_returns_name_if_empty_alias() {
        val column = createColumn("PAPERONE", "")
        assertEquals("PAPERONE", column.outName)
    }

    @Test
    fun getNameWithAlias_returns_name_when_no_alias() {
        val column = createColumn("PAPEROGA", null)
        assertEquals("PAPEROGA", column.nameWithAlias)
    }

    @Test
    fun getNameWithAlias_returns_name_when_alias_is_the_same() {
        val column = createColumn("PIPPO", "PIPPO")
        assertEquals("PIPPO", column.nameWithAlias)
    }

    @Test
    fun getNameWithAlias_returns_name_and_alias_when_different() {
        val column = createColumn("PAPERINO", "PAPERINIK")
        assertEquals("PAPERINO AS PAPERINIK", column.nameWithAlias)
    }

    @Test
    fun `two columns with same attributes are equal`() {
        val column1 = createColumn("Number 1", "ONE")
        column1.formatSource = "sorcio"
        column1.isAggregate = true
        column1.isDerived = false
        column1.type = "INT"

        val column2 = createColumn("Number 1", "ONE")
        column2.formatSource = "sorcio"
        column2.isAggregate = true
        column2.isDerived = false
        column2.type = "INT"

        assertEquals(column1, column2)
    }

    companion object {
        internal fun createColumn(name: String, alias: String?): Column {
            val column = Column(name)
            column.alias = alias
            return column
        }
    }
}
