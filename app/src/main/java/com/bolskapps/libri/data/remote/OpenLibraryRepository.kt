package com.bolskapps.libri.data.remote

import com.bolskapps.libri.data.CoverSize
import retrofit2.HttpException
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

/**
 * Why a search failed, so the UI can say something useful instead of one generic
 * "no connection" for every case.
 */
enum class SearchErrorKind {
    /** No usable connection, DNS failure, timeout. */
    NETWORK,

    /** Open Library throttled us (HTTP 429). Backing off is the only fix. */
    RATE_LIMITED,

    /** Open Library answered, but with an error status. */
    SERVER
}

class OpenLibraryException(
    val kind: SearchErrorKind,
    cause: Throwable? = null
) : IOException("Open Library request failed: $kind", cause)

class OpenLibraryRepository(private val api: OpenLibraryApi) {

    /**
     * Returns a failure rather than throwing: a dead or throttled network is an
     * expected condition here, not a crash.
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
        } catch (e: HttpException) {
            val kind = when (e.code()) {
                HTTP_TOO_MANY_REQUESTS -> SearchErrorKind.RATE_LIMITED
                else -> SearchErrorKind.SERVER
            }
            Result.failure(OpenLibraryException(kind, e))
        } catch (e: IOException) {
            Result.failure(OpenLibraryException(SearchErrorKind.NETWORK, e))
        }
    }

    private companion object {
        const val MIN_QUERY_LENGTH = 2
        const val HTTP_TOO_MANY_REQUESTS = 429
    }
}

/** Maps the error back to its kind, defaulting to a network problem. */
fun Throwable.searchErrorKind(): SearchErrorKind =
    (this as? OpenLibraryException)?.kind ?: SearchErrorKind.NETWORK

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
