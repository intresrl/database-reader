package it.intre.code.database.reader.config

import it.intre.code.database.reader.filter.FilterContainer
import org.hamcrest.CoreMatchers.hasItems
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import javax.ws.rs.core.MultivaluedHashMap

class QueryProfileTest {

    private var filterContainer = FilterContainer()
    private var queryProfile = QueryProfile()

    @BeforeEach
    fun setUp() {
        val map = MultivaluedHashMap<String, String>()
        map.putSingle("fruit", "KIWI")
        map.putSingle("color", "MAGENTA")
        map["animal"] = listOf("MOUSE", "CAT", "DOG")
        filterContainer.parseFrom(map)

    }

    @Test
    fun resolveMacroManagesExtraWhere() {
        val map = mapOf(
                "fruit" to "NVL(food,plant)='${ExtraWhereResolver.PLACEHOLDER}'",
                "color" to "UPPER(${ExtraWhereResolver.PLACEHOLDER}) IN (SELECT ${ExtraWhereResolver.PLACEHOLDER} FROM y)",
                "NOTHING" to "don't care"
        )
        queryProfile.extraWhere = map
        queryProfile.resolveMacro(filterContainer)

        val extraWhere = queryProfile.extraWhere
        assertNotNull(extraWhere)
        assertEquals(2, extraWhere!!.size, "WHERE conditions without values must be removed")
        assertThat(extraWhere.values, hasItems(
                "NVL(food,plant)='KIWI'",
                "UPPER(MAGENTA) IN (SELECT MAGENTA FROM y)"
        ))
    }

    @Test
    fun resolveMacroManagesExtraWhereWithLists() {
        val map = mapOf(
                "animal" to "beast IN ('' ###REPEAT{, '${ExtraWhereResolver.PLACEHOLDER}'}###)",
                "color" to "? > ?"
        )
        queryProfile.extraWhere = map
        queryProfile.resolveMacro(filterContainer)

        val extraWhere = queryProfile.extraWhere
        assertNotNull(extraWhere)
        assertEquals(2, extraWhere!!.size)
        assertThat(extraWhere.values, hasItems(
                "beast IN ('' , 'MOUSE', 'CAT', 'DOG')",
                "MAGENTA > MAGENTA"
        ))
    }

}