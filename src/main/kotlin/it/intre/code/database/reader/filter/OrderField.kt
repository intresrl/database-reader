package it.intre.code.database.reader.filter

/**
 * Sort options
 *
 * @author Giulio Roggero, Intré
 */
data class OrderField(var fieldId: String, var orderDirection: String) {

    override fun toString() = "$fieldId $orderDirection"

    companion object {
        val ASC = "ASC"
        val DESC = "DESC"
    }
}
