package it.intre.code.database.reader.util

import java.text.SimpleDateFormat
import java.util.TimeZone

object SqlDateManager {
    const val DEFAULT_TIMEZONE = "UTC+0"
    const val DEFAULT_DATE_FORMAT = "yyyy-MM-dd"

    @JvmStatic
    fun getFormatterFromFormatString(format: String?): SimpleDateFormat {
        val sourceDateFormat = if (!format.isNullOrEmpty()) format else DEFAULT_DATE_FORMAT
        val dateFormatter = SimpleDateFormat(sourceDateFormat)
        dateFormatter.timeZone = TimeZone.getTimeZone(DEFAULT_TIMEZONE)
        return dateFormatter
    }

    @JvmStatic
    fun fromTimestampToString(timestamp: Any?) = timestamp?.toString() ?: ""

}
