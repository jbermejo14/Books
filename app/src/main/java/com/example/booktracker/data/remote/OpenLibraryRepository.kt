package com.example.booktracker.data.remote

import com.example.booktracker.data.CoverSize
import java.io.IOException

/** A search hit, flattened into exactly what the add-book form consumes. */
data class BookSearchResult(
    val openLibraryKey: String,
    val title: String,
    val author: String,
    val coverId: Long?,
    val totalPages: Int?,
    val firstPublishYear: Int?
) {
    fun coverUrl(size: CoverSize = CoverSize.MEDIUM): String? =
        coverId?.let { "https://covers.openlibrary.org/b/id/$it-${size.suffix}.jpg" }
}

class OpenLibraryRepository(private val api: OpenLibraryApi) {

    /**
     * Returns a failure instead of throwing so the ViewModel can render an inline
     * error state — a dead network is an expected condition here, not a crash.
     */
    suspend fun search(query: String): Result<List<BookSearchResult>> {
        val trimmed = query.trim()
        if (trimmed.length < MIN_QUERY_LENGTH) return Result.success(emptyList())

        return try {
            val response = api.search(
                query = trimmed,
                fields = OpenLibraryApi.SEARCH_FIELDS,
                limit = OpenLibraryApi.SEARCH_LIMIT
            )
            // distinctBy: the work key is used as a LazyColumn item key, and a
            // duplicate would crash the list rather than just render twice.
            Result.success(
                response.docs
                    .mapNotNull { it.toSearchResult() }
                    .distinctBy { it.openLibraryKey }
            )
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: retrofit2.HttpException) {
            Result.failure(e)
        }
    }

    private companion object {
        const val MIN_QUERY_LENGTH = 2
    }
}

/** Drops documents with no title or key — they can't become a library entry. */
private fun OpenLibraryDoc.toSearchResult(): BookSearchResult? {
    val key = key ?: return null
    val title = title?.takeIf { it.isNotBlank() } ?: return null
    return BookSearchResult(
        openLibraryKey = key,
        title = title,
        author = authorName?.firstOrNull()?.takeIf { it.isNotBlank() } ?: "Unknown author",
        coverId = coverId,
        totalPages = numberOfPagesMedian?.takeIf { it > 0 },
        firstPublishYear = firstPublishYear
    )
}
