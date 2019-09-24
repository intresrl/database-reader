package it.intre.code.database.reader.config

import org.apache.commons.lang3.StringUtils.countMatches
import java.util.regex.Pattern.compile
import java.util.regex.Pattern.quote

/**
 * This class manages replacements of placeholders with actual values in
 * [QueryProfile.extraWhere] conditions.
 */
internal object ExtraWhereResolver {

    /**
     * Placeholder for value(s) to be replaced
     */
    const val PLACEHOLDER = "?"

    /**
     * Each part of the string between this marker and [.REPEAT_END]
     * will be repeated once for each value provided.
     * E.g. with values ["A","B","C"] the condition "foo=0 ###REPEAT{ and ?=1 }###"
     * will be resolved to "foo=0  and A=1 and B=1 and C=1"
     */
    const val REPEAT_START = "###REPEAT{"

    /**
     * Final marker of repeated strings, see [.REPEAT_START]
     */
    const val REPEAT_END = "}###"

    private val REPEAT_PATTERN = compile("(.*)" + quote(REPEAT_START) + "(.*)" + quote(REPEAT_END) + "(.*)")

    /**
     * Replace [.PLACEHOLDER] with provided value(s).
     * If the clause contains markers [.REPEAT_START] and [.REPEAT_END], the string
     * between them will be repeated for each value; otherwise, only first value is used.
     * Repetition markers should appear at most once and in order;
     * they will be ignored otherwise.
     *
     *
     * E.g. with values ["A","B","C"]:
     * - "foo > ?" becomes "foo > A"
     * - "bar < 0###REPEAT{+?}###" becomes "bar < 0+A+B+C"
     * - "###REPEAT{is this}### ###REPEAT{wrong ?}###" becomes "###REPEAT{is this}### ###REPEAT{wrong A}###"
     */
    fun resolve(condition: String, values: List<String>): String {
        val countStart = countMatches(condition, REPEAT_START)
        val countEnd = countMatches(condition, REPEAT_END)
        val matcher = REPEAT_PATTERN.matcher(condition)
        return if (countStart == 1 && countEnd == 1 && matcher.matches()) {
            val prefix = matcher.group(1)
            val repeat = matcher.group(2)
            val suffix = matcher.group(3)
            joinToString(prefix, repeat, suffix, values)
        } else {
            val replacement = if (values.isEmpty()) "" else values[0]
            condition.replace(PLACEHOLDER, replacement)
        }
    }

    private fun joinToString(prefix: String, repeat: String, suffix: String, values: List<String>): String {
        val result = StringBuilder()
        result.append(prefix)
        for (value in values) {
            result.append(repeat.replace(PLACEHOLDER, value))
        }
        result.append(suffix)
        return result.toString()
    }
}
