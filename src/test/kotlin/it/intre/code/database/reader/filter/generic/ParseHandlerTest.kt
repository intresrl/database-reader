package it.intre.code.database.reader.filter.generic

import it.intre.code.database.reader.TestArguments
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class ParseHandlerTest {

    companion object {
        @JvmStatic
        fun getArguments(): Stream<Arguments> = listOf<Arguments>(
                TestArguments.of(true, listOf("true")),
                TestArguments.of(true, listOf("TRUE")),
                TestArguments.of(true, listOf("True", "tRuE", "TRue")),
                TestArguments.of(false, listOf("false")),
                TestArguments.of(false, listOf("FALSE", "true")),
                TestArguments.of(false, listOf("a", "c;", "DXS")),
                TestArguments.of(false, emptyList<String>()),
                TestArguments.of(false, listOf(null, "true"))
        ).stream()
    }

    @ParameterizedTest
    @MethodSource("getArguments")
    fun `Returns true only if each element is "true" ✅`(expected: Boolean, list: List<String>) {
        Assertions.assertEquals(expected, ParseHandler.toBoolean(list))
    }


    private fun toBoolean(vararg s: String?): Boolean {
        return ParseHandler.toBoolean(listOf(*s))
    }


}