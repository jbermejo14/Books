package com.bolskapps.libri.data

/**
 * The four shelves from the "Reading Status" segmented control in the
 * `nuevo_libro_libri` design. Order matches the control, left to right.
 */
enum class ReadingStatus {
    /** Wanted, not owned yet — the Wishlist tab. */
    WISHLIST,

    /** Owned and queued — the "To Read" section of Pendientes. */
    TO_READ,

    /** Actively engaged — the "In Progress" section. */
    READING,

    /** Done — the History tab. */
    FINISHED;

    companion object {
        fun fromName(value: String?): ReadingStatus =
            entries.firstOrNull { it.name == value } ?: TO_READ
    }
}
