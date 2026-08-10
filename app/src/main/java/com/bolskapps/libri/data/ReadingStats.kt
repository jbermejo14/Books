package com.bolskapps.libri.data

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.ceil

/** How many days of sessions the pace average looks back over. */
const val PACE_WINDOW_DAYS = 30L

/** Derived reading figures. Pure data — everything here is computed, nothing stored. */
data class ReadingStats(
    /** Consecutive days with at least one session, counting back from today. */
    val currentStreakDays: Int = 0,
    val pagesThisWeek: Int = 0,
    /** Average pages per day over [PACE_WINDOW_DAYS], counting only days actually read. */
    val pagesPerActiveDay: Float = 0f,
    /** Average across every day in the window, including days with no reading. */
    val pagesPerDay: Float = 0f,
    val activeDaysInWindow: Int = 0
) {
    val hasPace: Boolean get() = pagesPerDay > 0f
}

/**
 * Builds [ReadingStats] from a session list.
 *
 * Kept free of Android and of the clock so it can be tested against fixed timestamps;
 * the caller supplies both [now] and the [zone] whose midnight defines a "day".
 */
fun computeReadingStats(
    sessions: List<ReadingSession>,
    now: Long,
    zone: ZoneId = ZoneId.systemDefault()
): ReadingStats {
    if (sessions.isEmpty()) return ReadingStats()

    val today = now.toLocalDate(zone)
    val windowStart = today.minusDays(PACE_WINDOW_DAYS - 1)

    val pagesByDay = sessions
        .groupBy { it.recordedAt.toLocalDate(zone) }
        .mapValues { (_, daySessions) -> daySessions.sumOf { it.pagesRead } }

    val weekStart = today.minusDays(6)
    val pagesThisWeek = pagesByDay
        .filterKeys { !it.isBefore(weekStart) && !it.isAfter(today) }
        .values.sum()

    val windowPages = pagesByDay
        .filterKeys { !it.isBefore(windowStart) && !it.isAfter(today) }
    val activeDays = windowPages.count { it.value > 0 }
    val windowTotal = windowPages.values.sum()

    return ReadingStats(
        currentStreakDays = streakLength(pagesByDay.keys, today),
        pagesThisWeek = pagesThisWeek,
        pagesPerActiveDay = if (activeDays == 0) 0f else windowTotal.toFloat() / activeDays,
        pagesPerDay = windowTotal.toFloat() / PACE_WINDOW_DAYS,
        activeDaysInWindow = activeDays
    )
}

/**
 * Counts back from today while days are consecutive.
 *
 * A streak that ends yesterday still counts: the day is not over, so someone who read
 * last night should not be told at 9am that they lost it.
 */
private fun streakLength(daysRead: Set<LocalDate>, today: LocalDate): Int {
    val anchor = when {
        daysRead.contains(today) -> today
        daysRead.contains(today.minusDays(1)) -> today.minusDays(1)
        else -> return 0
    }

    var streak = 0
    var cursor = anchor
    while (daysRead.contains(cursor)) {
        streak++
        cursor = cursor.minusDays(1)
    }
    return streak
}

/**
 * Days left to finish at the reader's recent pace, or null when there is nothing to
 * project from — no pace yet, no page count, or already finished.
 */
fun Book.daysToFinish(stats: ReadingStats): Int? {
    if (totalPages <= 0 || status == ReadingStatus.FINISHED) return null
    val remaining = totalPages - currentPage
    if (remaining <= 0) return null

    // Pace per *active* day answers "how many more reading days", which is the
    // question a reader actually asks — not "how many calendar days at my average".
    val pace = stats.pagesPerActiveDay
    if (pace <= 0f) return null

    return ceil(remaining / pace).toInt().coerceAtLeast(1)
}

/** Start of [year] in [zone], as epoch millis. */
fun startOfYear(year: Int, zone: ZoneId = ZoneId.systemDefault()): Long =
    LocalDate.of(year, 1, 1).atStartOfDay(zone).toInstant().toEpochMilli()

fun currentYear(now: Long, zone: ZoneId = ZoneId.systemDefault()): Int =
    now.toLocalDate(zone).year

private fun Long.toLocalDate(zone: ZoneId): LocalDate =
    Instant.ofEpochMilli(this).atZone(zone).toLocalDate()
