package com.example.booktracker.di

import android.content.Context
import com.example.booktracker.data.BookDatabase
import com.example.booktracker.data.BookRepository
import com.example.booktracker.data.remote.OpenLibraryApi
import com.example.booktracker.data.remote.OpenLibraryRepository
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

/**
 * Manual dependency container — small enough that Hilt would be overhead.
 * Everything is lazy so the Retrofit stack is only built if a search actually happens.
 */
interface AppContainer {
    val bookRepository: BookRepository
    val openLibraryRepository: OpenLibraryRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                // Open Library asks clients to identify themselves so they can contact
                // you before rate-limiting: https://openlibrary.org/developers/api
                val request = chain.request().newBuilder()
                    .header("User-Agent", USER_AGENT)
                    .build()
                chain.proceed(request)
            }
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(OpenLibraryApi.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    private val openLibraryApi: OpenLibraryApi by lazy {
        retrofit.create(OpenLibraryApi::class.java)
    }

    override val bookRepository: BookRepository by lazy {
        BookRepository(BookDatabase.getDatabase(context).bookDao())
    }

    override val openLibraryRepository: OpenLibraryRepository by lazy {
        OpenLibraryRepository(openLibraryApi)
    }

    private companion object {
        const val USER_AGENT = "BookTracker/1.0 (Android; contact via app store listing)"
    }
}
