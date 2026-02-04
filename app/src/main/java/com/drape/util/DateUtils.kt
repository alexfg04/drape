package com.drape.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Utility object for date formatting and parsing.
 * Uses "yyyy-MM-dd" format for PlannedDay storage.
 */
object DateUtils {
    private const val DATE_PATTERN = "yyyy-MM-dd"
    
    private val formatter: SimpleDateFormat
        get() = SimpleDateFormat(DATE_PATTERN, Locale.getDefault())

    /**
     * Formats a Date to "yyyy-MM-dd" string.
     */
    fun format(date: Date): String = formatter.format(date)

    /**
     * Returns today's date as "yyyy-MM-dd" string.
     */
    fun today(): String = format(Date())

    /**
     * Formats a Calendar to "yyyy-MM-dd" string.
     */
    fun format(calendar: Calendar): String = format(calendar.time)

    /**
     * Formats year, month, day to "yyyy-MM-dd" string.
     * Note: month is 0-indexed (January = 0)
     */
    fun format(year: Int, month: Int, dayOfMonth: Int): String {
        val calendar = Calendar.getInstance().apply {
            set(year, month, dayOfMonth)
        }
        return format(calendar)
    }

    /**
     * Parses a "yyyy-MM-dd" string to Date.
     * Returns null if parsing fails.
     */
    fun parse(dateString: String): Date? {
        return try {
            formatter.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Returns the date N days from today.
     */
    fun daysFromToday(days: Int): String {
        val calendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, days)
        }
        return format(calendar)
    }

    /**
     * Returns yesterday's date.
     */
    fun yesterday(): String = daysFromToday(-1)

    /**
     * Returns tomorrow's date.
     */
    fun tomorrow(): String = daysFromToday(1)

    /**
     * Returns first day of current month.
     */
    fun firstDayOfMonth(): String {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
        }
        return format(calendar)
    }

    /**
     * Returns last day of current month.
     */
    fun lastDayOfMonth(): String {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
        }
        return format(calendar)
    }
}
