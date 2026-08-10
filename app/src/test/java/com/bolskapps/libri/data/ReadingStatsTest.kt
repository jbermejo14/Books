package com.bolskapps.libri.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class ReadingStatsTest {

    private val zone: ZoneId = ZoneId.of("UTC")
    private val today = LocalDate.of(2026, 8, 10)
    private val now = today.atTime(20, 0).toEpochMilli()

    private fun LocalDateTime.toEpochMilli(): Long =
        atZone(zone).toInstant().toEpochMilli()

    /** A session [daysAgo] days before "today", at midday. */
    private fun session(daysAgo: Long, pages: Int, bookId: Int = 1) = ReadingSession(
        bookId = bookId,
        recordedAt = today.minusDays(daysAgo).atTime(12, 0).toEpochMilli(),
        pagesRead = pages,
        endPage = pages
    )

    private fun stats(vararg sessions: ReadingSession) =
        computeReadingStats(sessions.toList(), now, zone)

    @Test
    fun `no sessions means no stats`() {
        val result = computeReadingStats(emptyList(), now, zone)

        assertEquals(0, result.currentStreakDays)
        assertEquals(0, result.pagesThisWeek)
        assertEquals(0f, result.pagesPerDay, 0f)
    }

    @Test
    fun `consecutive days build a streak`() {
        val result = stats(session(0, 10), session(1, 10), session(2, 10))

        assertEquals(3, result.currentStreakDays)
    }

    @Test
    fun `a gap ends the streak`() {
        // Read today, yesterday, then nothing three days ago.
        val result = stats(session(0, 10), session(1, 10), session(3, 10))

        assertEquals(2, result.currentStreakDays)
    }

    @Test
    fun `a streak ending yesterday still counts`() {
        // The day is not over — telling someone at 8pm that they lost a streak they
        // could still save would be wrong.
        val result = stats(session(1, 10), session(2, 10))

        assertEquals(2, result.currentStreakDays)
    }

    @Test
    fun `a streak that ended two days ago is broken`() {
        val result = stats(session(2, 10), session(3, 10))

        assertEquals(0, result.currentStreakDays)
    }

    @Test
    fun `several sessions on one day count as a single streak day`() {
        val result = stats(session(0, 10), session(0, 15), session(1, 5))

        assertEquals(2, result.currentStreakDays)
        assertEquals(30, result.pagesThisWeek)
    }

    @Test
    fun `pages this week covers today and the six days before it`() {
        val result = stats(session(0, 10), session(6, 10), session(7, 999))

        // The 7-days-ago session falls outside the window.
        assertEquals(20, result.pagesThisWeek)
    }

    @Test
    fun `pace per active day ignores days without reading`() {
        val result = stats(session(0, 100), session(10, 100))

        assertEquals(100f, result.pagesPerActiveDay, 0.01f)
        assertEquals(2, result.activeDaysInWindow)
    }

    @Test
    fun `pace per calendar day averages across the whole window`() {
        val result = stats(session(0, 300))

        assertEquals(300f / PACE_WINDOW_DAYS, result.pagesPerDay, 0.01f)
    }

    @Test
    fun `sessions older than the window do not affect the pace`() {
        val result = stats(session(40, 500))

        assertEquals(0f, result.pagesPerActiveDay, 0f)
        assertEquals(0, result.activeDaysInWindow)
    }

    // ---- Finish estimates -------------------------------------------------

    private fun book(current: Int, total: Int, status: ReadingStatus = ReadingStatus.READING) =
        Book(
            title = "T",
            author = "A",
            currentPage = current,
            totalPages = total,
            status = status
        )

    @Test
    fun `finish estimate divides the remainder by the active-day pace`() {
        val pace = stats(session(0, 50), session(1, 50))

        // 100 pages left at 50 pages an active day.
        assertEquals(2, book(200, 300).daysToFinish(pace))
    }

    @Test
    fun `a partial day still counts as a day`() {
        val pace = stats(session(0, 50))

        // 60 pages left at 50/day rounds up to 2, not down to 1.
        assertEquals(2, book(240, 300).daysToFinish(pace))
    }

    @Test
    fun `no estimate without a pace`() {
        assertNull(book(10, 300).daysToFinish(ReadingStats()))
    }

    @Test
    fun `no estimate without a page count`() {
        val pace = stats(session(0, 50))

        assertNull(book(0, 0).daysToFinish(pace))
    }

    @Test
    fun `no estimate for a finished book`() {
        val pace = stats(session(0, 50))

        assertNull(book(300, 300, ReadingStatus.FINISHED).daysToFinish(pace))
    }

    // ---- Year boundaries --------------------------------------------------

    @Test
    fun `year helpers agree on the boundary`() {
        val start = startOfYear(2026, zone)

        assertEquals(2026, currentYear(start, zone))
        // One millisecond earlier is still the previous year.
        assertEquals(2025, currentYear(start - 1, zone))
    }
}
