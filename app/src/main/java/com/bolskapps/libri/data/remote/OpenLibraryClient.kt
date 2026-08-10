package com.bolskapps.libri.data.remote

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

/**
 * Builds the Open Library HTTP stack. Kept separate from the DI container so tests can
 * point the same configuration — same Json settings, same converter — at a local
 * server. A test against a hand-rolled parser would prove nothing about production.
 */
object OpenLibraryClient {

    /**
     * `ignoreUnknownKeys` matters: the search endpoint returns extra top-level fields
     * (`num_found`, `documentation_url`, …) and adds more over time. Strict parsing
     * would turn a harmless upstream addition into a total search outage.
     */
    val json: Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    fun okHttpClient(userAgent: String): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            chain.proceed(
                chain.request().newBuilder()
                    .header("User-Agent", userAgent)
                    .build()
            )
        }
        .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    fun api(
        baseUrl: String = OpenLibraryApi.BASE_URL,
        userAgent: String,
        client: OkHttpClient = okHttpClient(userAgent)
    ): OpenLibraryApi = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(OpenLibraryApi::class.java)

    private const val CONNECT_TIMEOUT_SECONDS = 15L
    private const val READ_TIMEOUT_SECONDS = 20L
}
