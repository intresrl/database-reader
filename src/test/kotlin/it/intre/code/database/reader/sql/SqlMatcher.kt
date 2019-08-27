package it.intre.code.database.reader.sql

import org.hamcrest.BaseMatcher
import org.hamcrest.Description

/**
 * Matches strings ignoring all whitespaces
 */
class SqlMatcher private constructor(private val expected: String) : BaseMatcher<String>() {

    override fun matches(actual: Any): Boolean {
        return normalize(expected).equals(normalize(actual.toString()), ignoreCase = true)
    }

    override fun describeTo(description: Description) {
        description.appendText(String.format("\"%s\" (should match without whitespaces)", expected))
    }

    companion object {

        fun matchesSql(expected: String): SqlMatcher {
            return SqlMatcher(expected)
        }

        private fun normalize(expected: String): String {
            return SqlHelper.normalizeSpaces(expected).toUpperCase()
        }
    }

}