package it.intre.code.database.reader.config

import it.intre.code.database.reader.config.ExtraWhereResolver.resolve
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import java.util.Collections.emptyList

class ExtraWhereResolverTest {

    private val values = listOf("A", "B", "C")

    @Test
    fun resolveWithNoRepeat() {
        assertThat(
                resolve("foo > ?", values),
                equalTo("foo > A")
        )
    }

    @Test
    fun resolveWithNoRepeatAndEmptyList() {
        assertThat(
                resolve("foo > ?", emptyList()),
                equalTo("foo > ")
        )
    }

    @Test
    fun resolveWithRepeatAndList() {
        assertThat(
                resolve("bar < 0###REPEAT{+?}###", values),
                equalTo("bar < 0+A+B+C")
        )
    }

    @Test
    fun resolveWithRepeatAndEmptyList() {
        assertThat(
                resolve("bar < 0###REPEAT{+?}###", emptyList()),
                equalTo("bar < 0")
        )
    }

    @Test
    fun resolveWithWrongRepeat() {
        assertThat(
                resolve("###REPEAT{is this}### ###REPEAT{wrong ?}###", values),
                equalTo("###REPEAT{is this}### ###REPEAT{wrong A}###")
        )
    }
}