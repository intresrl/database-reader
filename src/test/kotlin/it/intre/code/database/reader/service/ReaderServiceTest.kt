package it.intre.code.database.reader.service

import it.intre.code.database.reader.config.Column
import it.intre.code.database.reader.dao.ReaderDao
import it.intre.code.database.reader.filter.FilterContainer
import it.intre.code.database.reader.resultset.ReaderResultSet
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Matchers.any
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
@RunWith(JUnitPlatform::class)
class ReaderServiceTest {

    @InjectMocks
    internal var readerService: ReaderService? = null
    @Mock
    private val readerDao: ReaderDao? = null

    private var error: ReaderResultSet? = null
    private var success: ReaderResultSet? = null
    private var filter: FilterContainer? = null
    private var exception: RuntimeException? = null

    @BeforeAll
    fun setUp() {
        error = ReaderResultSet()
        error!!.isError = true
        error!!.errorMessage = "D'OH!"

        success = ReaderResultSet()
        success!!.columns = listOf(Column(), Column())
        success!!.list = listOf(
                mapOf("A" to "Alpha", "B" to "Beta"),
                mapOf("A" to "Aleph", "B" to "Beth")
        )

        filter = FilterContainer()
        exception = RuntimeException("D'OH!")
    }

    @Test
    fun findOk() {
        `when`(readerDao!!.find(any())).thenReturn(success)
        assertEquals(success, readerService!!.find(filter!!))
    }

    @Test
    fun getColumnsOk() {
        `when`(readerDao!!.getColumns(any())).thenReturn(success)
        assertEquals(success, readerService!!.getColumns(filter!!))
    }

    @Test
    fun findKo() {
        `when`(readerDao!!.find(any())).thenThrow(exception)
        assertEquals(error, readerService!!.find(filter!!))
    }

    @Test
    fun getColumnsKo() {
        `when`(readerDao!!.getColumns(any())).thenThrow(exception)
        assertEquals(error, readerService!!.getColumns(filter!!))
    }
}
