package com.bolskapps.libri.ui.navigation

import android.net.Uri
import com.bolskapps.libri.data.ReadingStatus

/** Every route in the app, in one place so the strings can't drift apart. */
object BookDestinations {

    const val DASHBOARD_ROUTE = "dashboard"
    const val PENDING_ROUTE = "pending"
    const val ADD_BOOK_ROUTE = "add_book"

    /** Open Library discovery — the primary way books enter the library. */
    const val SEARCH_ROUTE = "search"

    const val QUERY_ARG = "query"

    /** The query is optional: arriving with none lands on the suggestions view. */
    const val SEARCH_PATTERN = "$SEARCH_ROUTE?$QUERY_ARG={$QUERY_ARG}"

    /** Encoded, so a subject containing "&" or "/" can't break the route. */
    fun searchRoute(query: String? = null): String =
        if (query.isNullOrBlank()) SEARCH_ROUTE
        else "$SEARCH_ROUTE?$QUERY_ARG=${Uri.encode(query)}"

    const val STATUS_ARG = "status"

    /**
     * Wishlist and History render the same shelf grid, but they get separate route
     * patterns on purpose: one shared pattern would make them a single destination id,
     * and the bottom bar's saveState/restoreState would then serve one tab's list to
     * the other.
     */
    const val WISHLIST_ROUTE = "wishlist"
    const val HISTORY_ROUTE = "history"

    const val WISHLIST_PATTERN = "$WISHLIST_ROUTE?$STATUS_ARG={$STATUS_ARG}"
    const val HISTORY_PATTERN = "$HISTORY_ROUTE?$STATUS_ARG={$STATUS_ARG}"

    val WISHLIST_STATUS: String = ReadingStatus.WISHLIST.name
    val HISTORY_STATUS: String = ReadingStatus.FINISHED.name
}
