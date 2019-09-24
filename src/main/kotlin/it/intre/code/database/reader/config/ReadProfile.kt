package it.intre.code.database.reader.config

import java.util.Collections.emptyList

/**
 * Model for a read profile, which may contain one or more queries.
 */
data class ReadProfile(var name: String? = null, var queries: List<QueryProfile>? = null) {

    val columns: List<Column>?
        get() = if (queries!!.isEmpty()) {
            emptyList()
        } else queries!![0].columns

}
