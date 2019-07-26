package it.intre.code.database.reader.config

interface Column {
    /**
     * @return [.getAlias] if set, [.getName] otherwise
     */
    val outName: String

    var alias: String

    var name: String

    var type: String

    var isAggregate: Boolean

    var isDerived: Boolean

    var formatSource: String
}
