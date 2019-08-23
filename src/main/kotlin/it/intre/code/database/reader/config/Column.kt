package it.intre.code.database.reader.config

/**
 * Model of a DB column as read in a profile.
 */
data class Column(val name: String = "") {

    var alias: String? = null
    var type: String? = null

    /**
     * True if it's the result of an aggregate function, e.g. SUM(...) or COUNT(...)
     */
    var isAggregate: Boolean = false
    /**
     * True if it's derived from other fields/calculation, e.g. NVL(field1, field2) or TO_CHAR(field3, field4)||'-'||field5
     */
    var isDerived: Boolean = false
    /**
     * Specify format. Available for dates, e.g. "yyyy-MM-dd HH:mm"
     */
    var formatSource: String? = ""

    /**
     * @return [.getAlias] if set, [.getName] otherwise
     */
    val outName: String
        get() = if (alias.isNullOrEmpty()) name else alias!!

    /**
     * @return [.getAlias] if set, [.getName] otherwise
     */
    val nameWithAlias: String
        get() = name + if (alias.isNullOrEmpty() || alias == name) "" else " AS " + alias!!

    enum class NameGenerator {
        ONLY_NAME_NO_DERIVED {
            override fun generate(col: Column): String {
                return if (col.isDerived) "" else col.name
            }
        },
        ONLY_ALIAS {
            override fun generate(col: Column): String {
                return col.outName
            }
        },
        NAME_WITH_ALIAS {
            override fun generate(col: Column): String {
                return col.nameWithAlias
            }
        };

        abstract fun generate(col: Column): String
    }
}
