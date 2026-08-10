package com.bolskapps.libri.data.remote

import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Exercises the production HTTP stack ([OpenLibraryClient]) against a local server, so
 * the serializer configuration under test is the one that actually ships.
 */
class OpenLibraryRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var repository: OpenLibraryRepository

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        repository = OpenLibraryRepository(
            OpenLibraryClient.api(
                baseUrl = server.url("/").toString(),
                userAgent = "Libri/test"
            )
        )
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun enqueue(code: Int, body: String = "") {
        server.enqueue(MockResponse().setResponseCode(code).setBody(body))
    }

    @Test
    fun `parses a real search payload`() = runTest {
        enqueue(200, REAL_PAYLOAD)

        val results = repository.search("dune").getOrThrow()

        assertEquals(2, results.size)
        val first = results.first()
        assertEquals("/works/OL893516W", first.openLibraryKey)
        assertEquals("Children of Dune", first.title)
        assertEquals("Frank Herbert", first.author)
        assertEquals(6976407L, first.coverId)
        assertEquals(504, first.totalPages)
        assertEquals(1976, first.firstPublishYear)
        assertEquals(
            "https://covers.openlibrary.org/b/id/6976407-M.jpg",
            first.coverUrl()
        )
    }

    @Test
    fun `unknown upstream fields do not break parsing`() = runTest {
        // Open Library adds top-level and per-doc fields over time; strict parsing would
        // turn a harmless upstream change into a total search outage.
        enqueue(
            200,
            """
            {"numFound":1,"start":0,"brand_new_field":{"x":1},"docs":[
              {"key":"/works/OL1W","title":"A","author_name":["B"],"future_field":[1,2]}
            ]}
            """.trimIndent()
        )

        val results = repository.search("anything").getOrThrow()

        assertEquals(1, results.size)
        assertEquals("A", results.first().title)
    }

    @Test
    fun `documents without a title or key are dropped`() = runTest {
        enqueue(
            200,
            """
            {"numFound":3,"docs":[
              {"key":"/works/OL1W","title":"Keeper","author_name":["Someone"]},
              {"key":"/works/OL2W"},
              {"title":"No key"}
            ]}
            """.trimIndent()
        )

        val results = repository.search("partial").getOrThrow()

        assertEquals(1, results.size)
        assertEquals("Keeper", results.first().title)
    }

    @Test
    fun `duplicate work keys are collapsed`() = runTest {
        // The key backs a LazyColumn item key — a duplicate crashes the list.
        enqueue(
            200,
            """
            {"numFound":2,"docs":[
              {"key":"/works/OL1W","title":"Same","author_name":["A"]},
              {"key":"/works/OL1W","title":"Same","author_name":["A"]}
            ]}
            """.trimIndent()
        )

        val results = repository.search("dupes").getOrThrow()

        assertEquals(1, results.size)
    }

    @Test
    fun `missing author and page data degrade gracefully`() = runTest {
        enqueue(200, """{"numFound":1,"docs":[{"key":"/works/OL9W","title":"Sparse"}]}""")

        val result = repository.search("sparse").getOrThrow().single()

        assertEquals("Unknown author", result.author)
        assertNull(result.totalPages)
        assertNull(result.coverId)
        assertNull(result.coverUrl())
    }

    @Test
    fun `a zero page count is treated as no page count`() = runTest {
        enqueue(
            200,
            """{"numFound":1,"docs":[{"key":"/works/OL9W","title":"Z","number_of_pages_median":0}]}"""
        )

        assertNull(repository.search("zero").getOrThrow().single().totalPages)
    }

    @Test
    fun `429 is reported as rate limiting rather than a generic failure`() = runTest {
        enqueue(429)

        val error = repository.search("throttled").exceptionOrNull()

        assertEquals(SearchErrorKind.RATE_LIMITED, error?.searchErrorKind())
    }

    @Test
    fun `a server error is distinguished from a dead network`() = runTest {
        enqueue(500)

        val error = repository.search("broken").exceptionOrNull()

        assertEquals(SearchErrorKind.SERVER, error?.searchErrorKind())
    }

    @Test
    fun `an unreachable server surfaces as a network error`() = runTest {
        server.shutdown()

        val error = repository.search("offline").exceptionOrNull()

        assertTrue(error is OpenLibraryException)
        assertEquals(SearchErrorKind.NETWORK, error?.searchErrorKind())
    }

    @Test
    fun `short queries never reach the network`() = runTest {
        val results = repository.search("a").getOrThrow()

        assertTrue(results.isEmpty())
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `the request asks only for the fields the form needs`() = runTest {
        enqueue(200, """{"numFound":0,"docs":[]}""")

        repository.search("dune")

        val url = server.takeRequest().requestUrl!!
        assertEquals("dune", url.queryParameter("q"))
        assertEquals(OpenLibraryApi.SEARCH_FIELDS, url.queryParameter("fields"))
        assertEquals(
            OpenLibraryApi.SEARCH_LIMIT.toString(),
            url.queryParameter("limit")
        )
    }

    @Test
    fun `the client identifies itself to Open Library`() = runTest {
        enqueue(200, """{"numFound":0,"docs":[]}""")

        repository.search("dune")

        val userAgent = server.takeRequest().getHeader("User-Agent")
        assertEquals("Libri/test", userAgent)
    }

    private companion object {
        /** Trimmed from a live `search.json` response, field names verified upstream. */
        val REAL_PAYLOAD = """
            {
              "numFound": 45123,
              "start": 0,
              "numFoundExact": true,
              "num_found": 45123,
              "documentation_url": "https://openlibrary.org/search.json",
              "q": "dune",
              "offset": null,
              "docs": [
                {
                  "author_name": ["Frank Herbert"],
                  "cover_i": 6976407,
                  "first_publish_year": 1976,
                  "key": "/works/OL893516W",
                  "number_of_pages_median": 504,
                  "title": "Children of Dune"
                },
                {
                  "author_name": ["Frank Herbert"],
                  "cover_i": 6711531,
                  "first_publish_year": 1981,
                  "key": "/works/OL893514W",
                  "number_of_pages_median": 475,
                  "title": "God Emperor of Dune"
                }
              ]
            }
        """.trimIndent()
    }
}
