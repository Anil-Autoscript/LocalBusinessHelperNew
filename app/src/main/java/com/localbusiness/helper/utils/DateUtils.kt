package com.localbusiness.helper.utils

import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeParseException

object DateUtils {

    private val displayFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

    private val parsers = listOf(
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("dd-MM-yyyy"),
        DateTimeFormatter.ofPattern("MM/dd/yyyy"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("dd MMM yyyy"),
        DateTimeFormatter.ofPattern("d/M/yyyy"),
        DateTimeFormatter.ofPattern("d-M-yyyy")
    )

    fun formatDate(timestamp: Long): String {
        if (timestamp == 0L) return ""
        return try {
            displayFormatter.format(millisToLocalDate(timestamp))
        } catch (e: Exception) {
            ""
        }
    }

    fun parseDate(dateStr: String): Long {
        if (dateStr.isBlank()) return 0L
        for (formatter in parsers) {
            try {
                val date = LocalDate.parse(dateStr.trim(), formatter)
                return localDateToLong(date)
            } catch (e: DateTimeParseException) {
                continue
            }
        }
        return 0L
    }

    fun todayRange(): Pair<Long, Long> {
        val today = LocalDate.now(ZoneId.systemDefault())
        val start = localDateToLong(today)
        val end = today.atTime(23, 59, 59)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        return Pair(start, end)
    }

    fun longToLocalDate(timestamp: Long): LocalDate? {
        if (timestamp == 0L) return null
        return try {
            millisToLocalDate(timestamp)
        } catch (e: Exception) {
            null
        }
    }

    fun localDateToLong(date: LocalDate): Long {
        return date.atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    // ThreeTenABP-safe conversion — works on API 24+
    private fun millisToLocalDate(millis: Long): LocalDate {
        return Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }
}
