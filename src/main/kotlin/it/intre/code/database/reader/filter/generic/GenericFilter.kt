package it.intre.code.database.reader.filter.generic

/** Generic custom filter */
abstract class GenericFilter(
        /** Name of the field */
        open var name: String,
        /**
         * If true, it means that any value is valid
         * (ignore other conditions except [.isNegate]).
         */
        @get:JvmName("isAll")
        open var all: Boolean,
        /**
         * If true, it means that the filter is inverted
         * (e.g. it matches no values at all if [.isAll] is true).
         */
        @get:JvmName("isNegate")
        open var negate: Boolean
) {

    /**
     * @return true only if this filter is creating a meaningful condition
     */
    abstract fun hasCondition(): Boolean

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GenericFilter) return false

        if (name != other.name) return false
        if (all != other.all) return false
        if (negate != other.negate) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + all.hashCode()
        result = 31 * result + negate.hashCode()
        return result
    }
}
