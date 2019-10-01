package it.intre.code.database.reader.resultset

import it.intre.code.database.reader.config.Column
import it.intre.code.database.reader.filter.FilterContainer
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class ReaderResultSetTest {

    @Test
    fun `durationNanoSeconds returns the difference between start and end times - 0`() {
        val mock = { 100L }
        val readerResultSet = ReaderResultSet(mock, null, 0)
        readerResultSet.start()
        readerResultSet.end()
        assertEquals(0L, readerResultSet.durationNanoSeconds)
    }

    @Test
    fun `durationNanoSeconds returns the difference between start and end times - 94368`() {
        var tmp = 100000L
        val mock = { tmp += 94368; tmp }
        val readerResultSet = ReaderResultSet(mock, null, 0)
        readerResultSet.start()
        readerResultSet.end()
        assertEquals(94368L, readerResultSet.durationNanoSeconds)
    }

    @Test
    fun `durationNanoSeconds returns the difference between start and end times divided by 1 million`() {
        var tmp = 0XDEADBEEFL
        val mock = { tmp += 9876543210; tmp }
        val readerResultSet = ReaderResultSet(mock, null, 0)
        readerResultSet.start()
        readerResultSet.end()
        assertEquals(9876L, readerResultSet.durationMilliSeconds)
    }

    @Test
    fun `minimize remove large data properties`() {
        val list = listOf(mapOf("X" to 1), mapOf("Y" to 2, "Z" to 3))
        val readerResultSet = ReaderResultSet({1L}, list, 2)
        readerResultSet.sql="SELECT BLA FROM BLA"
        readerResultSet.filter = FilterContainer()
        readerResultSet.columns = listOf(Column("gigi"))

        readerResultSet.minimize()

        assertNull(readerResultSet.columns)
        assertNull(readerResultSet.filter)
        assertEquals("", readerResultSet.sql)
    }
}
