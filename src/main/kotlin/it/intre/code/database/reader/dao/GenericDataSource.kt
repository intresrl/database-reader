package it.intre.code.database.reader.dao

import java.sql.Connection

interface GenericDataSource {

    val connection: Connection

}
