package com.bolskapps.libri.di

import android.content.Context
import com.bolskapps.libri.data.BookDatabase
import com.bolskapps.libri.data.BookRepository
import com.bolskapps.libri.data.remote.OpenLibraryApi
import com.bolskapps.libri.data.remote.OpenLibraryClient
import com.bolskapps.libri.data.remote.OpenLibraryRepository

/**
 * Manual dependency container — small enough that Hilt would be overhead.
 * Everything is lazy so the HTTP stack is only built if a search actually happens.
 */
interface AppContainer {
    val bookRepository: BookRepository
    val openLibraryRepository: OpenLibraryRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    private val openLibraryApi: OpenLibraryApi by lazy {
        OpenLibraryClient.api(userAgent = USER_AGENT)
    }

    override val bookRepository: BookRepository by lazy {
        BookRepository(BookDatabase.getDatabase(context).bookDao())
    }

    override val openLibraryRepository: OpenLibraryRepository by lazy {
        OpenLibraryRepository(openLibraryApi)
    }

    private companion object {
        // Open Library asks clients to identify themselves with a reachable contact so
        // they can get in touch before rate-limiting: https://openlibrary.org/developers/api
        const val USER_AGENT = "Libri/1.0 (Android; +bolskapps@gmail.com)"
    }
}
