package it.intre.code.database.reader.filter

/**
 * Sort options
 */
data class OrderField(var fieldId: String = "", var orderDirection: String = "" ) {

    override fun toString() = "$fieldId $orderDirection"

    companion object {
        const val ASC = "ASC"
        const val DESC = "DESC"
    }
}
