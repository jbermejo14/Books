package com.example.booktracker.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Open Library search. Public, key-less, rate-limited by courtesy — so we always ask
 * for an explicit `fields` list instead of pulling the full (very large) documents.
 *
 * https://openlibrary.org/dev/docs/api/search
 */
interface OpenLibraryApi {

    @GET("search.json")
    suspend fun search(
        @Query("q") query: String,
        @Query("fields") fields: String,
        @Query("limit") limit: Int
    ): OpenLibrarySearchResponse

    companion object {
        const val BASE_URL = "https://openlibrary.org/"

        /** Everything the add-book form needs, and nothing else. */
        const val SEARCH_FIELDS =
            "key,title,author_name,cover_i,number_of_pages_median,first_publish_year"

        const val SEARCH_LIMIT = 25
    }
}

@Serializable
data class OpenLibrarySearchResponse(
    val numFound: Int = 0,
    val docs: List<OpenLibraryDoc> = emptyList()
)

@Serializable
data class OpenLibraryDoc(
    /** Work key, e.g. `/works/OL45883W`. */
    val key: String? = null,
    val title: String? = null,
    @SerialName("author_name") val authorName: List<String>? = null,
    @SerialName("cover_i") val coverId: Long? = null,
    @SerialName("number_of_pages_median") val numberOfPagesMedian: Int? = null,
    @SerialName("first_publish_year") val firstPublishYear: Int? = null
)
