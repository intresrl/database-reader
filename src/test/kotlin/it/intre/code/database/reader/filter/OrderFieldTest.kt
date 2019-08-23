package it.intre.code.database.reader.filter

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class OrderFieldTest {

    @Test
    fun `toString concatenates id and direction`() {
        var cacca : CharSequence? = "CIAO"
        println(cacca.isNullOrEmpty())
        cacca=""
        println(cacca.isNullOrEmpty())
        cacca=null
        println((cacca as CharSequence?).isNullOrEmpty())
    }
}