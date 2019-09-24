package it.intre.code.database.reader.sql


/**
 * Builder of SQL queries
 */
internal object SqlHelper {

    // TODO mettere tutti i metodi di istanza

    const val TRUE = "1=1"

    /**
     * Remove all whitespaces between non-words; replace all whitespaces between words with a single space.
     */
    fun normalizeSpaces(sql: String): String {
        return sql
                .replace("\\s+".toRegex(), " ")
                .replace("\\B | \\B".toRegex(), "")
    }

}
