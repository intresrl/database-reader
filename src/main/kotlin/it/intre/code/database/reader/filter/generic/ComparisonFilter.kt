package it.intre.code.database.reader.filter.generic

open class ComparisonFilter(
        override var name: String,
        override var all: Boolean = false,
        override var negate: Boolean = false,
        /** Greater than */
        var gt: Any? = null,
        /** Greater than or equal to */
        var ge: Any? = null,
        /** Less than */
        var lt: Any? = null,
        /** Less than or equal to */
        var le: Any? = null,
        /** [.DATATYPE_STRING] (default if null) or #DATATYPE_INTEGER */
        var datatype: String? = null
) : GenericFilter(name, all, negate) {

    override fun hasCondition() = !hasNoCondition()

    fun hasNoCondition() = all && !negate || ge == null && gt == null && le == null && lt == null

    override fun toString(): String {
        return "ComparisonFilter(name='$name', all=$all, negate=$negate, gt=$gt, ge=$ge, lt=$lt, le=$le, datatype=$datatype)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ComparisonFilter

        if (name != other.name) return false
        if (all != other.all) return false
        if (negate != other.negate) return false
        if (gt != other.gt) return false
        if (ge != other.ge) return false
        if (lt != other.lt) return false
        if (le != other.le) return false
        if (datatype != other.datatype) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + all.hashCode()
        result = 31 * result + negate.hashCode()
        result = 31 * result + (gt?.hashCode() ?: 0)
        result = 31 * result + (ge?.hashCode() ?: 0)
        result = 31 * result + (lt?.hashCode() ?: 0)
        result = 31 * result + (le?.hashCode() ?: 0)
        result = 31 * result + (datatype?.hashCode() ?: 0)
        return result
    }


    companion object {
        const val DATATYPE_STRING = "String"
        const val DATATYPE_INTEGER = "Integer"
    }


}
