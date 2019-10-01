package it.intre.code.database.reader.service

import it.intre.code.database.reader.config.Column
import it.intre.code.database.reader.dao.ReaderDao
import it.intre.code.database.reader.filter.FilterContainer
import it.intre.code.database.reader.resultset.ReaderResultSet
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

class ReaderServiceTest {

    private val readerDao = mock(ReaderDao::class.java)

    private val error = ReaderResultSet().apply {
        isError = true
        errorMessage = "D'OH!"
    }

    private val success = ReaderResultSet().apply {
        columns = listOf(Column("One"), Column("Two"))
        list = listOf(
                mapOf("A" to "Alpha", "B" to "Beta"),
                mapOf("A" to "Aleph", "B" to "Beth")
        )
    }

    private val filter = FilterContainer()

    private val exception = RuntimeException("D'OH!")

    @Test
    fun findOk() {
        `when`(readerDao.find(filter)).thenReturn(success)
        assertEquals(success, ReaderService.make(readerDao).find(filter))
    }

    @Test
    fun getColumnsOk() {
        `when`(readerDao.getColumns(filter)).thenReturn(success)
        assertEquals(success, ReaderService.make(readerDao).getColumns(filter))
    }

    @Test
    fun findKo() {
        `when`(readerDao.find(filter)).thenThrow(exception)
        assertEquals(error, ReaderService.make(readerDao).find(filter))
    }

    @Test
    fun getColumnsKo() {
        `when`(readerDao.getColumns(filter)).thenThrow(exception)
        assertEquals(error, ReaderService.make(readerDao).getColumns(filter))
    }
}
