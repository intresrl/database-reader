package it.intre.code.database.reader.filter

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class OrderFieldTest {

    @Test
    fun `toString concatenates id and direction`() {
        val orderField = OrderField("deploy", "value")
        assertEquals("deploy value", orderField.toString())
    }
}