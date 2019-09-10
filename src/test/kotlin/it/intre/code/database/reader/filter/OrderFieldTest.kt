package it.intre.code.database.reader.filter

import it.intre.code.database.reader.TestArguments
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class OrderFieldTest {

    companion object {
        @JvmStatic
        fun getArguments(): Stream<Arguments> = listOf<Arguments>(
                TestArguments.of(" ", OrderField()),
                TestArguments.of("id ", OrderField("id")),
                TestArguments.of("id order", OrderField("id","order")),
                TestArguments.of(" order", OrderField(orderDirection = "order"))
        ).stream()
    }

    @ParameterizedTest(name = "[{1}->{0}]")
    @MethodSource("getArguments")
    fun `toString concatenates id and direction`(expected: String, orderField: OrderField) {
        assertEquals(expected, orderField.toString())
    }
}