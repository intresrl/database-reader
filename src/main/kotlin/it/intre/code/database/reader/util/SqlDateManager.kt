package it.intre.code.database.reader.util

import java.text.SimpleDateFormat
import java.util.TimeZone

object SqlDateManager {
    private const val DEFAULT_TIMEZONE = "UTC+0"
    private const val DEFAULT_DATE_FORMAT = "yyyy-MM-dd"

    fun getFormatterFromFormatString(format: String?): SimpleDateFormat {
        val sourceDateFormat = if (!format.isNullOrEmpty()) format else DEFAULT_DATE_FORMAT
        val dateFormatter = SimpleDateFormat(sourceDateFormat)
        dateFormatter.timeZone = TimeZone.getTimeZone(DEFAULT_TIMEZONE)
        return dateFormatter
    }

    fun fromTimestampToString(timestamp: Any?) = timestamp?.toString() ?: ""

}
