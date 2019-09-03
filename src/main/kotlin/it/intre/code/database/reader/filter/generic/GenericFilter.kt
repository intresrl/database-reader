package it.intre.code.database.reader.filter.generic

/** Generic custom filter */
abstract class GenericFilter(
        /** Name of the field */
        open var name: String,
        /**
         * If true, it means that any value is valid
         * (ignore other conditions except [.isNegate]).
         */
        open var all: Boolean,
        /**
         * If true, it means that the filter is inverted
         * (e.g. it matches no values at all if [.isAll] is true).
         */
        open var negate: Boolean
) {

    /**
     * @return true only if this filter is creating a meaningful condition
     */
    abstract fun hasCondition(): Boolean
}
