package it.intre.code.database.reader.config

import it.intre.code.database.reader.filter.FilterContainer
import org.hamcrest.CoreMatchers.hasItems
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ProfileLoaderIT {

    companion object {
        const val MOCK1 = "1-mock"
        const val MOCK2 = "2-mock-with-include"
    }

    private fun toUrl(path: String) = with("$path.json") {
        Thread.currentThread().contextClassLoader?.getResource(this) ?: ClassLoader.getSystemResource(this)
    }

    @Test
    fun loadProfile() {
        val profileLoader = ProfileLoader()
        val filter = FilterContainer()

        filter.profile = MOCK1

        val readProfile = profileLoader.loadProfile(filter, ::toUrl)

        assertEquals("PROFILE_NAME", readProfile!!.name)

        val queries = readProfile.queries
        assertEquals(1, queries!!.size)

        val query = queries[0]
        assertEquals("TABLE_NAME", query.table)
        assertEquals("field = 0", query.where)
        assertEquals("", query.group)
        assertEquals("", query.having)
        assertEquals("name2 desc", query.order)
        assertEquals(2, query.columns!!.size)

        val extraWhere = query.extraWhere
        assertEquals(2, extraWhere!!.size)
        assertThat(extraWhere.keys, hasItems<String>("custom1", "custom2"))
    }

    @Test
    fun loadProfileWithInclude() {
        val profileLoader = ProfileLoader()
        val filter = FilterContainer()
        filter.profile = MOCK2
        val readProfile = profileLoader.loadProfile(filter, ::toUrl)
        assertEquals("mock2", readProfile!!.name)

        val queries = readProfile.queries
        assertEquals(1, queries!!.size)

        val query = queries[0]
        assertEquals("table2", query.table)
        assertEquals("2-include", query.include)
        assertEquals("e", query.where)
        assertEquals("f", query.group)
        assertEquals("g", query.having)
        assertEquals("h", query.order)
        assertEquals(1, query.columns!!.size)

        val extraWhere = query.extraWhere
        assertEquals(1, extraWhere!!.size)
        assertEquals("j", extraWhere["i"])
    }
}
