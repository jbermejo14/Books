package com.example.booktracker.ui.navigation

import com.example.booktracker.data.ReadingStatus

/** Every route in the app, in one place so the strings can't drift apart. */
object BookDestinations {

    const val DASHBOARD_ROUTE = "dashboard"
    const val PENDING_ROUTE = "pending"
    const val ADD_BOOK_ROUTE = "add_book"

    /** Open Library discovery — the primary way books enter the library. */
    const val SEARCH_ROUTE = "search"

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
