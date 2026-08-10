package com.bolskapps.libri.data

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * [normalized] is the single gate every database write passes through, so these are the
 * rules the whole library depends on staying true.
 */
class BookNormalizationTest {

    private fun book(
        currentPage: Int = 0,
        totalPages: Int = 300,
        status: ReadingStatus = ReadingStatus.TO_READ,
        rating: Int = 0,
        notes: String? = null,
        title: String = "Dune",
        author: String = "Frank Herbert",
        genre: String? = null
    ) = Book(
        title = title,
        author = author,
        currentPage = currentPage,
        totalPages = totalPages,
        status = status,
        rating = rating,
        notes = notes,
        genre = genre
    )

    @Test
    fun `current page is clamped above the total`() {
        val result = book(currentPage = 5_000, totalPages = 300).normalized()
        assertEquals(300, result.currentPage)
    }

    @Test
    fun `negative current page is clamped to zero`() {
        val result = book(currentPage = -20).normalized()
        assertEquals(0, result.currentPage)
    }

    @Test
    fun `reaching the last page promotes the book to finished`() {
        val result = book(currentPage = 300, totalPages = 300, status = ReadingStatus.READING)
            .normalized()
        assertEquals(ReadingStatus.FINISHED, result.status)
    }

    @Test
    fun `dropping below the last page demotes a finished book back to reading`() {
        val result = book(currentPage = 120, totalPages = 300, status = ReadingStatus.FINISHED)
            .normalized()
        assertEquals(ReadingStatus.READING, result.status)
    }

    @Test
    fun `any progress moves a to-read book to reading`() {
        val result = book(currentPage = 1, status = ReadingStatus.TO_READ).normalized()
        assertEquals(ReadingStatus.READING, result.status)
    }

    @Test
    fun `an untouched to-read book stays put`() {
        val result = book(currentPage = 0, status = ReadingStatus.TO_READ).normalized()
        assertEquals(ReadingStatus.TO_READ, result.status)
    }

    @Test
    fun `a wishlist entry is never promoted by page data`() {
        // Wishlist is aspirational; stray page counts must not move it to another shelf.
        val result = book(currentPage = 300, totalPages = 300, status = ReadingStatus.WISHLIST)
            .normalized()
        assertEquals(ReadingStatus.WISHLIST, result.status)
    }

    @Test
    fun `rating and notes survive on every shelf`() {
        val result = book(status = ReadingStatus.WISHLIST, rating = 4, notes = "Looks great")
            .normalized()
        assertEquals(4, result.rating)
        assertEquals("Looks great", result.notes)
    }

    @Test
    fun `rating is clamped to the allowed range`() {
        assertEquals(MAX_RATING, book(rating = 99).normalized().rating)
        assertEquals(0, book(rating = -3).normalized().rating)
    }

    @Test
    fun `blank notes and genre collapse to null rather than empty strings`() {
        val result = book(notes = "   ", genre = "  ").normalized()
        assertEquals(null, result.notes)
        assertEquals(null, result.genre)
    }

    @Test
    fun `title and author are trimmed`() {
        val result = book(title = "  Dune  ", author = "  Frank Herbert ").normalized()
        assertEquals("Dune", result.title)
        assertEquals("Frank Herbert", result.author)
    }

    @Test
    fun `a book with no page count cannot report progress`() {
        // Wishlist entries often arrive from Open Library with no page data at all.
        val result = book(currentPage = 40, totalPages = 0).normalized()
        assertEquals(0, result.currentPage)
        assertEquals(0f, result.progress, 0f)
        assertEquals(0, result.progressPercent)
    }

    @Test
    fun `progress is a fraction of the page count`() {
        val result = book(currentPage = 150, totalPages = 300).normalized()
        assertEquals(0.5f, result.progress, 0.001f)
        assertEquals(50, result.progressPercent)
    }
}
