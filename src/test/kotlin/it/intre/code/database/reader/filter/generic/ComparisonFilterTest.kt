package it.intre.code.database.reader.filter.generic

import it.intre.code.database.reader.TestArguments
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream


internal class ComparisonFilterTest {

    companion object {
        @JvmStatic
        fun getListComparisonFilter(): Stream<Arguments> = listOf<Arguments>(
                TestArguments.of(false, ComparisonFilter("", all = false, negate = false)),
                TestArguments.of(true, ComparisonFilter("", all = false, negate = false, gt = 1)),
                TestArguments.of(true, ComparisonFilter("", all = false, negate = false, ge = 2)),
                TestArguments.of(true, ComparisonFilter("", all = false, negate = false, lt = 2)),
                TestArguments.of(true, ComparisonFilter("", all = false, negate = false, le = 2)),
                TestArguments.of(false, ComparisonFilter("", all = false, negate = false, datatype = "C")),
                TestArguments.of(false, ComparisonFilter("", all = true, negate = false)),
                TestArguments.of(false, ComparisonFilter("", all = true, negate = false, gt = "X")),
                TestArguments.of(true, ComparisonFilter("", all = true, negate = true, ge = "X"))
        ).stream()
    }

    @ParameterizedTest
    @MethodSource("getListComparisonFilter")
    fun `hasCondition()`(expected: Boolean, comparisonFilter: ComparisonFilter) {
        assertEquals(expected, comparisonFilter.hasCondition())
    }
}
