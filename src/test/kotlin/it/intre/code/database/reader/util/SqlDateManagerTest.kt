package it.intre.code.database.reader.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.text.SimpleDateFormat
import java.util.*

internal class SqlDateManagerTest {

    private val DEFAULT_DATE_FORMAT = "yyyy-MM-dd"
    private val DEFAULT_TIMEZONE = "UTC+0"

    @Test
    fun `null format returns default formatter`() {
        val actual = SqlDateManager.getFormatterFromFormatString(null)
        val expected = SimpleDateFormat(DEFAULT_DATE_FORMAT)
        expected.timeZone = TimeZone.getTimeZone(DEFAULT_TIMEZONE)
        assertEquals(expected, actual)
    }

    @Test
    fun `empty format returns default formatter`() {
        val actual = SqlDateManager.getFormatterFromFormatString("")
        val expected = SimpleDateFormat(DEFAULT_DATE_FORMAT)
        expected.timeZone = TimeZone.getTimeZone(DEFAULT_TIMEZONE)
        assertEquals(expected, actual)
    }

    @Test
    fun `valid format returns formatter`() {
        val actual = SqlDateManager.getFormatterFromFormatString("dd/MM/yyyy")
        val expected = SimpleDateFormat("dd/MM/yyyy")
        expected.timeZone = TimeZone.getTimeZone(DEFAULT_TIMEZONE)
        assertEquals(expected, actual)
    }

    @Test
    fun `long timestamp return string timestamp`(){
        val actual = SqlDateManager.fromTimestampToString(999999L)
        assertEquals("999999", actual)
    }

    @Test
    fun `null timestamp return empty string`() {
        val actual = SqlDateManager.fromTimestampToString(null)
        assertEquals("", actual)
    }

    @Test
    fun `empty timestamp return empty string`() {
        val actual = SqlDateManager.fromTimestampToString("")
        assertEquals("", actual)
    }

    @Test
    fun `string timestamp return string`() {
        val actual = SqlDateManager.fromTimestampToString("E STRING")
        assertEquals("E STRING", actual)
    }

    @Test
    fun `Object return string`() {
        val actual = SqlDateManager.fromTimestampToString(object{
            override fun toString() = "HELLO"})
        assertEquals("HELLO", actual)
    }
}
