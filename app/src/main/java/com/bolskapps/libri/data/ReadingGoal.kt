package com.bolskapps.libri.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * How many books the reader wants to finish in a given year. One row per year, so past
 * years keep the target they were actually set — changing this year's goal must not
 * rewrite history.
 */
@Entity(tableName = "reading_goals")
data class ReadingGoal(
    @PrimaryKey
    val year: Int,
    val targetBooks: Int
)

/** Offered on the goal picker; the reader can still type any number. */
val GOAL_PRESETS = listOf(6, 12, 24, 52)

const val MAX_GOAL_BOOKS = 999
